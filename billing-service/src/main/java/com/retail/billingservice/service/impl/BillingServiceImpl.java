package com.retail.billingservice.service.impl;

import com.retail.billingservice.client.CustomerClient;
import com.retail.billingservice.client.SalesClient;
import com.retail.billingservice.dto.customer.CustomerDto;
import com.retail.billingservice.dto.sales.SaleDto;
import com.retail.billingservice.dto.sales.SalePaymentDto;
import com.retail.billingservice.dto.sales.SaleStatus;
import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import com.retail.billingservice.exception.CustomerNotFoundException;
import com.retail.billingservice.exception.InvoiceAlreadyExistsException;
import com.retail.billingservice.exception.InvalidInvoiceException;
import com.retail.billingservice.exception.InvoiceNotFoundException;
import com.retail.billingservice.exception.SaleNotFinalizedException;
import com.retail.billingservice.exception.SaleNotFoundException;
import com.retail.billingservice.mapper.BillingMapper;
import com.retail.billingservice.model.InvoiceStatus;
import com.retail.billingservice.model.PaymentMethod;
import com.retail.billingservice.model.PaymentStatus;
import com.retail.billingservice.repository.InvoiceRepository;
import com.retail.billingservice.service.BillingService;
import com.retail.billingservice.service.InvoiceSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private static final String DEFAULT_INVOICE_PREFIX = "INV";
    private static final String DEFAULT_CURRENCY = "INR";

    private final InvoiceRepository invoiceRepository;
    private final SalesClient salesClient;
    private final CustomerClient customerClient;
    private final BillingMapper billingMapper;
    private final InvoiceSettingsService invoiceSettingsService;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Long saleId = request.getSaleId();

        // Check if invoice already exists for this sale
        invoiceRepository.findBySaleId(saleId).ifPresent(inv -> {
            throw new InvoiceAlreadyExistsException(
                    "Invoice already exists for sale ID: " + saleId);
        });

        // Fetch finalized sale from Sales Service
        SaleDto sale = salesClient.getFinalizedSale(saleId);
        if (sale == null) {
            throw new SaleNotFoundException(
                    "Sale not found or not finalized: " + saleId);
        }

        // Validate sale is finalized (status check)
        if (!isFinalized(sale)) {
            throw new SaleNotFinalizedException(
                    "Sale is not finalized: " + saleId);
        }

        // Verify customer exists
        boolean customerExists = customerClient.existsById(sale.getCustomerId());
        if (!customerExists) {
            throw new CustomerNotFoundException(
                    "Customer not found: " + sale.getCustomerId());
        }

        // Resolve the customer display name (first + last) from the customer
        // service so it can be snapshotted onto the invoice. Kept inline so no
        // read-time N+1 lookups are needed when invoices are listed. Null-safe:
        // if the lookup fails the invoice is still created without a name and the
        // frontend falls back to the customer id.
        String customerName = null;
        try {
            CustomerDto customer = customerClient.getCustomer(sale.getCustomerId());
            if (customer != null) {
                customerName = displayName(customer);
            }
        } catch (Exception e) {
            log.warn("Could not resolve customer name for id {}; continuing without it", sale.getCustomerId(), e);
        }

        // Load current settings so newly generated invoices use the configured
        // prefix and currency. A default settings row is seeded by Flyway.
        InvoiceSettingsResponse settings = invoiceSettingsService.getSettings();

        // Build invoice from sale
        Invoice invoice = Invoice.builder()
                .saleId(sale.getId())
                .saleNumber(sale.getSaleNumber())
                .customerId(sale.getCustomerId())
                .customerName(customerName)
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotalAmount())
                .currency(resolveCurrency(settings.getCurrency()))
                .invoiceStatus(InvoiceStatus.GENERATED)
                .build();

        // Map sale items to invoice items
        if (sale.getSaleItems() != null && !sale.getSaleItems().isEmpty()) {
            sale.getSaleItems().forEach(saleItem -> {
                InvoiceItem item = InvoiceItem.builder()
                        .productId(saleItem.getProductId())
                        .variantId(saleItem.getProductVariantId())
                        .sku(saleItem.getSku())
                        .barcode(saleItem.getBarcode())
                        .productName(saleItem.getProductName())
                        .quantity(saleItem.getQuantity())
                        .unitPrice(saleItem.getUnitPrice())
                        .discountAmount(saleItem.getDiscountAmount())
                        .taxAmount(saleItem.getTaxAmount())
                        .lineTotal(saleItem.getTotalAmount())
                        .build();
                invoice.addItem(item);
            });
        }

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber(settings.getInvoicePrefix());
        invoice.setInvoiceNumber(invoiceNumber);

        // Snapshot payment info from the finalized sale. The sale carries its
        // collected payments (amount, method, reference), so a fully paid sale
        // yields a PAID invoice instead of a GENERATED one with a zero paid
        // amount that would print "Paid 0" on the receipt.
        applyPaymentSnapshot(invoice, sale);

        // Save invoice and return response
        Invoice saved = invoiceRepository.save(invoice);
        return billingMapper.toInvoiceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found: " + invoiceId));
        return billingMapper.toInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found: " + invoiceNumber));
        return billingMapper.toInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(billingMapper::toInvoiceResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomer(Long customerId) {
        return billingMapper.toInvoiceResponses(
                invoiceRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceBySaleId(Long saleId) {
        Invoice invoice = invoiceRepository.findBySaleId(saleId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found for sale ID: " + saleId));
        return billingMapper.toInvoiceResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found: " + invoiceId));

        // Prevent cancelling an already cancelled invoice
        if (invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            throw new InvalidInvoiceException("Invoice is already cancelled: " + invoiceId);
        }

        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        Invoice updated = invoiceRepository.save(invoice);
        return billingMapper.toInvoiceResponse(updated);
    }

    private String generateInvoiceNumber(String configuredPrefix) {
        long sequence = invoiceRepository.nextInvoiceNumberSequence();
        int year = java.time.Year.now().getValue();
        return String.format("%s-%d-%06d", resolveInvoicePrefix(configuredPrefix), year, sequence);
    }

    private String resolveInvoicePrefix(String configuredPrefix) {
        if (configuredPrefix == null || configuredPrefix.isBlank()) {
            return DEFAULT_INVOICE_PREFIX;
        }
        String trimmed = configuredPrefix.trim();
        // A trailing dash is treated as the separator so both "AC-" and "AC"
        // render as "AC-2026-...". The placeholder default is a bare "INV".
        while (trimmed.endsWith("-")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? DEFAULT_INVOICE_PREFIX : trimmed;
    }

    private String resolveCurrency(String configuredCurrency) {
        if (configuredCurrency == null || configuredCurrency.isBlank()) {
            return DEFAULT_CURRENCY;
        }
        return configuredCurrency.trim();
    }

    private boolean isFinalized(SaleDto sale) {
        // A sale is finalized only when it is COMPLETED and fully paid. The
        // sales-service guarantees COMPLETED => PAID; checking both keeps
        // invoice creation defensive against any sale that reports a non-paid
        // state (e.g. legacy or inconsistent data).
        return sale != null
                && sale.getSaleStatus() == SaleStatus.COMPLETED
                && PaymentStatus.PAID.name().equals(sale.getPaymentStatus());
    }

    private String displayName(CustomerDto customer) {
        if (customer == null) {
            return null;
        }
        String first = customer.getFirstName() == null ? "" : customer.getFirstName().trim();
        String last = customer.getLastName() == null ? "" : customer.getLastName().trim();
        String name = (first + " " + last).trim();
        return name.isEmpty() ? null : name;
    }

    /**
     * Performs payment snapshot: sums the collected amounts, carries the first
     * payment's method and reference, and marks a fully collected invoice PAID.
     */
    private void applyPaymentSnapshot(Invoice invoice, SaleDto sale) {
        BigDecimal totalPaid = BigDecimal.ZERO;
        PaymentMethod method = null;
        String reference = null;

        List<SalePaymentDto> payments = sale.getPayments();
        if (payments != null) {
            for (SalePaymentDto payment : payments) {
                if (payment.getAmount() != null) {
                    totalPaid = totalPaid.add(payment.getAmount());
                }
                if (method == null && payment.getPaymentMethod() != null) {
                    method = mapPaymentMethod(payment.getPaymentMethod());
                }
                if (reference == null && payment.getTransactionReference() != null
                        && !payment.getTransactionReference().isBlank()) {
                    reference = payment.getTransactionReference();
                }
            }
        }

        invoice.setPaidAmount(totalPaid);
        invoice.setPaymentMethod(method);
        invoice.setTransactionReference(reference);

        if (totalPaid.signum() > 0 && totalPaid.compareTo(invoice.getTotal()) >= 0) {
            invoice.setPaymentStatus(PaymentStatus.PAID);
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
        } else if (totalPaid.signum() > 0) {
            invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            invoice.setPaymentStatus(PaymentStatus.PENDING);
        }
    }

    /**
     * Maps the sales service payment-method name (which has a wider enum) onto
     * the billing PaymentMethod subset. Unsupported methods resolve to null so
     * the receipt shows "N/A" instead of failing on enum conversion.
     */
    private PaymentMethod mapPaymentMethod(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim().toUpperCase()) {
            case "CASH" -> PaymentMethod.CASH;
            case "CARD" -> PaymentMethod.CARD;
            case "UPI" -> PaymentMethod.UPI;
            case "NET_BANKING", "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER;
            case "WALLET" -> PaymentMethod.WALLET;
            case "CHEQUE" -> PaymentMethod.CHEQUE;
            case "OTHER" -> PaymentMethod.OTHER;
            default -> null;
        };
    }

}
