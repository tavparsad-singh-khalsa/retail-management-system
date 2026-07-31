package com.retail.sales_service.service.impl;

import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustItemRequest;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;
import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.request.SaleItemRequest;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Payment;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.entity.SaleItem;
import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.enums.SaleStatus;
import com.retail.sales_service.exception.ProductServiceException;
import com.retail.sales_service.exception.InvalidSaleException;
import com.retail.sales_service.exception.PaymentValidationException;
import com.retail.sales_service.exception.SaleNotFoundException;
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.service.SaleService;
import com.retail.sales_service.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductClient productClient;

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
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

            BigDecimal unit = variant.getSellingPrice();
            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            SaleItem si = SaleItem.builder()
                    .productVariantId(itemReq.getProductVariantId())
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

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(tax);

        // Validate payments and build payment entities
        List<Payment> payments = new ArrayList<>();
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (request.getPayments() != null) {
            for (PaymentRequest pr : request.getPayments()) {
                if (pr.getAmount() == null || pr.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new PaymentValidationException("Payment amount must be > 0");
                }
                if (pr.getPaymentMethod() != PaymentMethod.CASH && (pr.getTransactionReference() == null || pr.getTransactionReference().isBlank())) {
                    throw new PaymentValidationException("transactionReference is required for non-cash payments");
                }
                totalPaid = totalPaid.add(pr.getAmount());
                Payment p = Payment.builder()
                        .paymentMethod(pr.getPaymentMethod())
                        .amount(pr.getAmount())
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

        // Deduct inventory BEFORE persisting the sale to ensure consistency
        String inventoryRef = "SAL-INIT-" + UUID.randomUUID();
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

        // Persist sale (cascade saves items and payments)
        Sale saved = saleRepository.save(sale);

      // generate sale number and persist update
        String saleNumber = String.format("SAL-%d-%06d", Year.now().getValue(), saved.getId());
        saved.setSaleNumber(saleNumber);
        saved = saleRepository.save(saved);

       return saleMapper.toResponse(saved);
    }

   @Override
    public SaleResponse getSaleById(Long saleId) {
        Sale s = saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleId));
        return saleMapper.toResponse(s);
    }

    @Override
    public List<SaleResponse> getAllSales() {
        List<Sale> all = saleRepository.findAll();
        return all.stream().map(saleMapper::toResponse).collect(Collectors.toList());
    }

   @Override
    @Transactional
    public SaleResponse cancelSale(Long saleId) {
        Sale s = saleRepository.findById(saleId).orElseThrow(() -> new SaleNotFoundException("Sale not found: " + saleId));
        s.setSaleStatus(SaleStatus.CANCELLED);
        saleRepository.save(s);
        return saleMapper.toResponse(s);
    }

   private PaymentStatus computePaymentStatus(BigDecimal total, BigDecimal totalPaid) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.PENDING;
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) return PaymentStatus.PENDING;
        if (totalPaid.compareTo(total) >= 0) return PaymentStatus.PAID;
        return PaymentStatus.PARTIALLY_PAID;
    }
}
