package com.retail.sales_service.service.impl;

import com.retail.sales_service.client.BillingClient;
import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustItemRequest;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.request.StockMovementRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.InventoryResponse;
import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;
import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.request.SaleItemRequest;
import com.retail.sales_service.dto.response.CreateSaleResult;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Payment;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.entity.SaleItem;
import com.retail.sales_service.enums.MovementType;
import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.enums.ReferenceType;
import com.retail.sales_service.enums.SaleStatus;
import com.retail.sales_service.exception.IdempotencyConflictException;
import com.retail.sales_service.exception.ProductServiceException;
import com.retail.sales_service.exception.InvalidSaleException;
import com.retail.sales_service.exception.PaymentValidationException;
import com.retail.sales_service.exception.SaleHasActiveInvoiceException;
import com.retail.sales_service.exception.SaleNotFoundException;
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.service.SaleService;
import com.retail.sales_service.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductClient productClient;
    private final BillingClient billingClient;

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        return createSale(request, null).response();
    }

    @Override
    @Transactional
    public CreateSaleResult createSale(CreateSaleRequest request, String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        String requestHash = requestFingerprint(request);

        // A retry after a successful operation must never touch inventory again:
        // resolve from the persisted sale state before doing any work.
        if (normalizedKey != null) {
            Optional<Sale> existing = saleRepository.findByIdempotencyKey(normalizedKey);
            if (existing.isPresent()) {
                return replayOrConflict(existing.get(), requestHash);
            }
        }

        // Basic validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidSaleException("Sale must contain at least one item");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SaleItem> saleItems = new ArrayList<>();
        List<InventoryAdjustItemRequest> adjustItems = new ArrayList<>();


        // Fetch product variants and prepare sale items + inventory adjustment
        for (SaleItemRequest itemReq : request.getItems()) {
            ProductVariantResponse variant = productClient.getVariantById(itemReq.getProductVariantId());
            if (variant == null) throw new ProductServiceException("Variant not found: " + itemReq.getProductVariantId());
            if (variant.getSellingPrice() == null) throw new InvalidSaleException("Variant has no selling price: " + variant.getId());

            BigDecimal unit = cents(variant.getSellingPrice());
            BigDecimal lineTotal = cents(unit.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            subtotal = subtotal.add(lineTotal);

            SaleItem si = SaleItem.builder()
                    .productVariantId(itemReq.getProductVariantId())
                    .productId(variant.getProductId())
                    .sku(variant.getSku())
                    .barcode(variant.getBarcode())
                    .productName(variant.getProductName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unit)
                    .discountAmount(BigDecimal.ZERO)
                    .taxAmount(BigDecimal.ZERO)
                    .totalAmount(lineTotal)
                    .build();
            saleItems.add(si);

            InventoryAdjustItemRequest ai = InventoryAdjustItemRequest.builder()
                    .productVariantId(itemReq.getProductVariantId())
                    .quantity(itemReq.getQuantity())
                    .build();
            adjustItems.add(ai);
        }

        BigDecimal discount = cents(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        BigDecimal taxableAmount = subtotal.subtract(discount);
        InvoiceSettingsResponse invoiceSettings = billingClient.getInvoiceSettings();
        BigDecimal tax = computeTax(taxableAmount, invoiceSettings);
        BigDecimal total = cents(taxableAmount.add(tax));

        // Validate payments and build payment entities
        List<Payment> payments = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (request.getPayments() != null) {
            for (PaymentRequest pr : request.getPayments()) {
                if (pr.getAmount() == null) {
                    throw new PaymentValidationException("Payment amount must be > 0");
                }
                BigDecimal amount = cents(pr.getAmount());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new PaymentValidationException("Payment amount must be > 0");
                }
                if (pr.getPaymentMethod() != PaymentMethod.CASH && (pr.getTransactionReference() == null || pr.getTransactionReference().isBlank())) {
                    throw new PaymentValidationException("transactionReference is required for non-cash payments");
                }
                totalPaid = totalPaid.add(amount);
                Payment p = Payment.builder()
                        .paymentMethod(pr.getPaymentMethod())
                        .amount(amount)
                        .transactionReference(pr.getTransactionReference())
                        .notes(pr.getNotes())
                        .paymentStatus(PaymentStatus.PENDING)
                        .build();
                payments.add(p);
            }
            if (totalPaid.compareTo(total) > 0) {
                throw new PaymentValidationException("Total paid exceeds sale total");
            }
        }

        // Full payment is required unless the invoice settings allow partial
        // payments. This enforcement stays authoritative regardless of the request.
        if (!Boolean.TRUE.equals(invoiceSettings == null ? null : invoiceSettings.getAllowPartialPayment())
                && totalPaid.compareTo(BigDecimal.ZERO) > 0
                && totalPaid.compareTo(total) < 0) {
            throw new PaymentValidationException(
                    "Partial payments are disabled. Full payment is required for this sale.");
        }

        // Deduct inventory BEFORE persisting the sale to ensure consistency.
        // When an Idempotency-Key is present the deduction reference is derived
        // from it so every retry of the same operation reaches Product Service
        // with the SAME reference; Product Service then replays that reference
        // instead of deducting stock a second time. Keyless legacy calls keep a
        // fresh reference (they carry no retry identity by design).
        String inventoryRef = "SAL-INIT-" + (normalizedKey != null ? normalizedKey : UUID.randomUUID());
        InventoryAdjustRequest adjustReq = InventoryAdjustRequest.builder()
                .referenceNumber(inventoryRef)
                .items(adjustItems)
                .build();

       InventoryOperationResponse opResp = productClient.deductInventory(adjustReq);
        if (opResp == null || !opResp.isSuccess()) {
            throw new ProductServiceException("Inventory deduction failed: " + (opResp == null ? "null response" : opResp.getMessage()));
        }

       // Build sale and attach items/payments (cascade will persist children)
        Sale sale = Sale.builder()
                .customerId(request.getCustomerId())
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(tax)
                .totalAmount(total)
                .notes(request.getNotes())
                .saleStatus(SaleStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .idempotencyKey(normalizedKey)
                .idempotencyRequestHash(normalizedKey == null ? null : requestHash)
                .build();

       // associate items and payments with sale
        saleItems.forEach(si -> si.setSale(sale));
        payments.forEach(p -> p.setSale(sale));
        sale.setSaleItems(saleItems);
        sale.setPayments(payments);

      // compute payment/sale status before save
        PaymentStatus computedPaymentStatus = computePaymentStatus(total, totalPaid);
        sale.setPaymentStatus(computedPaymentStatus);
        if (computedPaymentStatus == PaymentStatus.PAID && total.compareTo(BigDecimal.ZERO) > 0) {
            sale.setSaleStatus(SaleStatus.COMPLETED);
            payments.forEach(p -> p.setPaymentStatus(PaymentStatus.PAID));
        } else if (computedPaymentStatus == PaymentStatus.PARTIALLY_PAID) {
            sale.setSaleStatus(SaleStatus.CREATED);
            payments.forEach(p -> p.setPaymentStatus(PaymentStatus.PENDING));
        } else {
            payments.forEach(p -> p.setPaymentStatus(PaymentStatus.PENDING));
        }

        // Persist sale (cascade saves items and payments). A concurrent duplicate
        // request that raced past the lookup will be rejected by the unique index
        // on idempotency_key; resolve it from the persisted winner instead of
        // re-running inventory deduction.
        Sale saved;
        try {
            saved = saleRepository.save(sale);
        } catch (DataIntegrityViolationException e) {
            if (normalizedKey == null) {
                throw e;
            }
            return saleRepository.findByIdempotencyKey(normalizedKey)
                    .map(winner -> replayOrConflict(winner, requestHash))
                    .orElseThrow(() -> e);
        }

      // generate sale number. The entity is managed after save(), so assigning
        // the number here is flushed to the DB atomically with the insert when
        // the transaction commits - no second save is issued, and the persisted
        // row never carries a NULL sale_number.
        String saleNumber = String.format("SAL-%d-%06d", Year.now().getValue(), saved.getId());
        saved.setSaleNumber(saleNumber);

       return new CreateSaleResult(saleMapper.toResponse(saved), false);
    }

   @Override
    public SaleResponse getSaleById(Long saleId) {
        Sale s = saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleId));
        return saleMapper.toResponse(s);
    }

    @Override
    public Page<SaleResponse> getAllSales(Pageable pageable) {
        return saleRepository.findAll(pageable).map(saleMapper::toResponse);
    }

   @Override
    @Transactional
    public SaleResponse cancelSale(Long saleId) {
        Sale s = saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleId));

        // Check if sale is already cancelled
        if (s.getSaleStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleException("Sale is already cancelled: " + saleId);
        }

        // Check if sale has an active invoice
        if (billingClient.hasActiveInvoice(saleId)) {
            throw new SaleHasActiveInvoiceException(saleId);
        }

        // Restore inventory: aggregate quantities by variant so one deterministic
        // RETURN movement is created per (sale, variant). This mirrors the SALE
        // deduction aggregation (SalesIntegrationServiceImpl.aggregateItems) and
        // ensures RETURN-{saleNumber}-VAR-{variantId} is collision-free even if
        // the same variant appears as multiple SaleItem rows. The total restored
        // per variant equals the total originally deducted.
        if (s.getSaleItems() != null && !s.getSaleItems().isEmpty()) {
            Map<Long, Integer> quantityByVariant = new LinkedHashMap<>();
            for (SaleItem item : s.getSaleItems()) {
                quantityByVariant.merge(item.getProductVariantId(), item.getQuantity(), Integer::sum);
            }
            for (Map.Entry<Long, Integer> entry : quantityByVariant.entrySet()) {
                restoreInventoryForVariant(s, entry.getKey(), entry.getValue());
            }
        }

        s.setSaleStatus(SaleStatus.CANCELLED);
        saleRepository.save(s);
        return saleMapper.toResponse(s);
    }

    private void restoreInventoryForVariant(Sale sale, Long variantId, int totalQuantity) {
        // Get inventory for this variant
        InventoryResponse inventory = productClient.getInventoryByVariantId(variantId);
        if (inventory == null) {
            throw new ProductServiceException("Inventory not found for variant: " + variantId);
        }

        // Create RETURN stock movement to restore inventory using deterministic reference
        // Reference is stable per (saleNumber, variantId) so any retry produces the
        // same identity and product-service can replay idempotently.
        String referenceNumber = "RETURN-" + sale.getSaleNumber() + "-VAR-" + variantId;
        StockMovementRequest movementRequest = StockMovementRequest.builder()
                .inventoryId(inventory.getId())
                .movementType(MovementType.RETURN)
                .quantity(totalQuantity)
                .referenceNumber(referenceNumber)
                .remarks("Sale cancelled - restoring inventory")
                .build();

        InventoryOperationResponse response = productClient.createStockMovement(movementRequest);
        if (response == null || !response.isSuccess()) {
            throw new ProductServiceException("Failed to restore inventory for variant: " + variantId);
        }
    }

    @Deprecated
    private void restoreInventoryForItem(Sale sale, SaleItem item) {
        restoreInventoryForVariant(sale, item.getProductVariantId(), item.getQuantity());
    }

    @Override
    @Transactional
    public SaleResponse settlePayment(Long saleId, PaymentRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleId));

        if (sale.getSaleStatus() == SaleStatus.CANCELLED || sale.getSaleStatus() == SaleStatus.RETURNED) {
            throw new InvalidSaleException("Sale is not eligible for settlement: " + saleId);
        }
        if (sale.getPaymentStatus() == PaymentStatus.PAID) {
            throw new PaymentValidationException("Sale is already fully paid: " + saleId);
        }

        // Payment validation mirrors the createSale rules
        if (request.getAmount() == null) {
            throw new PaymentValidationException("Payment amount must be > 0");
        }
        BigDecimal amount = cents(request.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Payment amount must be > 0");
        }
        if (request.getPaymentMethod() != PaymentMethod.CASH
                && (request.getTransactionReference() == null || request.getTransactionReference().isBlank())) {
            throw new PaymentValidationException("transactionReference is required for non-cash payments");
        }

        // Outstanding balance is computed from the authoritative persisted
        // sale/payment state, never from a frontend-provided remaining balance.
        BigDecimal total = sale.getTotalAmount() == null ? BigDecimal.ZERO : sale.getTotalAmount();
        BigDecimal totalPaid = computeTotalPaid(sale);
        BigDecimal remaining = cents(total.subtract(totalPaid));
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Sale has no outstanding balance: " + saleId);
        }
        if (amount.compareTo(remaining) > 0) {
            throw new PaymentValidationException(
                    "Payment amount exceeds remaining balance: " + saleId + " (remaining: " + remaining + ")");
        }

        // Partial settlement is only allowed when invoice settings permit it
        if (amount.compareTo(remaining) < 0) {
            InvoiceSettingsResponse invoiceSettings = billingClient.getInvoiceSettings();
            if (!Boolean.TRUE.equals(invoiceSettings == null ? null : invoiceSettings.getAllowPartialPayment())) {
                throw new PaymentValidationException(
                        "Partial payments are disabled. Full payment is required for this sale.");
            }
        }

        // Settlement never deducts inventory, never touches sale items, and
        // never creates a second sale: only a new payment is appended.
        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .amount(amount)
                .transactionReference(request.getTransactionReference())
                .notes(request.getNotes())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        payment.setSale(sale);
        sale.getPayments().add(payment);

        BigDecimal newTotalPaid = totalPaid.add(amount);
        PaymentStatus computedPaymentStatus = computePaymentStatus(total, newTotalPaid);
        sale.setPaymentStatus(computedPaymentStatus);
        if (computedPaymentStatus == PaymentStatus.PAID && total.compareTo(BigDecimal.ZERO) > 0) {
            sale.setSaleStatus(SaleStatus.COMPLETED);
            sale.getPayments().forEach(p -> p.setPaymentStatus(PaymentStatus.PAID));
        } else if (computedPaymentStatus == PaymentStatus.PARTIALLY_PAID) {
            sale.setSaleStatus(SaleStatus.CREATED);
            sale.getPayments().forEach(p -> p.setPaymentStatus(PaymentStatus.PENDING));
        } else {
            sale.getPayments().forEach(p -> p.setPaymentStatus(PaymentStatus.PENDING));
        }

        Sale saved = saleRepository.save(sale);
        return saleMapper.toResponse(saved);
    }

    private BigDecimal computeTotalPaid(Sale sale) {
        if (sale.getPayments() == null || sale.getPayments().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sale.getPayments().stream()
                .filter(p -> p.getAmount() != null && !Boolean.FALSE.equals(p.getActive()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal cents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // Backend is authoritative for tax: the rate comes from the saved invoice
    // settings, never from the request. Tax is disabled when the billing
    // service is unreachable, the flag is off, or the rate is not positive.
    private BigDecimal computeTax(BigDecimal taxableAmount, InvoiceSettingsResponse settings) {
        if (settings == null || !Boolean.TRUE.equals(settings.getTaxEnabled())
                || settings.getTaxRate() == null || settings.getTaxRate().compareTo(BigDecimal.ZERO) <= 0) {
            return cents(BigDecimal.ZERO);
        }
        return cents(taxableAmount.multiply(settings.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

   // Trims the client key; blank or missing keys keep the legacy non-idempotent
    // path. Visible ASCII only and at most 64 chars (the persisted column width).
    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) return null;
        String trimmed = idempotencyKey.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > 64) {
            throw new InvalidSaleException("Idempotency-Key must be at most 64 characters");
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isWhitespace(c) || c < 0x21 || c > 0x7E) {
                throw new InvalidSaleException("Idempotency-Key may only contain visible ASCII characters");
            }
        }
        return trimmed;
    }

    // Deterministic SHA-256 fingerprint of the normalized request. Covers every
    // field that shapes the sale outcome: customer, items (in order), payments
    // (in order), discount and notes share the same, family-sensitive enough,
    // append-only serialization. request.taxAmount is deliberately excluded
    // because the backend recomputes tax from invoice settings and is
    // authoritative.
    private String requestFingerprint(CreateSaleRequest request) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("v2");
        sb.append("|customer=").append(request.getCustomerId() == null ? "" : request.getCustomerId());
        sb.append("|discount=").append(canonical(request.getDiscountAmount()));
        sb.append("|notes=").append(request.getNotes() == null ? "" : request.getNotes());
        sb.append("|items=");
        if (request.getItems() != null) {
            for (SaleItemRequest item : request.getItems()) {
                sb.append(item.getProductVariantId() == null ? "" : item.getProductVariantId());
                sb.append(':').append(item.getQuantity() == null ? "" : item.getQuantity());
                sb.append(',');
            }
        }
        sb.append("|payments=");
        if (request.getPayments() != null) {
            for (PaymentRequest payment : request.getPayments()) {
                sb.append(payment.getPaymentMethod() == null ? "" : payment.getPaymentMethod().name());
                sb.append(':').append(canonical(payment.getAmount()));
                sb.append(':').append(payment.getTransactionReference() == null ? "" : payment.getTransactionReference());
                sb.append(':').append(payment.getNotes() == null ? "" : payment.getNotes());
                sb.append(',');
            }
        }
        return sha256Hex(sb.toString());
    }

    private static String canonical(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    // Same key + same payload replays the persisted result (HTTP 200, no side
    // effects). Same key + different payload is a 409-conflict.
    private CreateSaleResult replayOrConflict(Sale existing, String requestHash) {
        if (existing.getIdempotencyRequestHash() != null
                && existing.getIdempotencyRequestHash().equals(requestHash)) {
            return new CreateSaleResult(saleMapper.toResponse(existing), true);
        }
        throw new IdempotencyConflictException(
                "Idempotency-Key was already used for a sale with different request content");
    }

   private PaymentStatus computePaymentStatus(BigDecimal total, BigDecimal totalPaid) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.PENDING;
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.PENDING;
        if (totalPaid.compareTo(total) >= 0) return PaymentStatus.PAID;
        return PaymentStatus.PARTIALLY_PAID;
    }
}
