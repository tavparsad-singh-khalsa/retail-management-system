package com.retail.billingservice.service.impl;

import com.retail.billingservice.client.CustomerClient;
import com.retail.billingservice.client.SalesClient;
import com.retail.billingservice.dto.sales.SaleDto;
import com.retail.billingservice.dto.sales.SaleStatus;
import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.request.MarkPaidRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import com.retail.billingservice.exception.CustomerNotFoundException;
import com.retail.billingservice.exception.InvalidPaymentException;
import com.retail.billingservice.exception.InvoiceAlreadyExistsException;
import com.retail.billingservice.exception.InvoiceAlreadyPaidException;
import com.retail.billingservice.exception.InvoiceNotFoundException;
import com.retail.billingservice.exception.SaleNotFinalizedException;
import com.retail.billingservice.exception.SaleNotFoundException;
import com.retail.billingservice.mapper.BillingMapper;
import com.retail.billingservice.model.InvoiceStatus;
import com.retail.billingservice.model.PaymentStatus;
import com.retail.billingservice.repository.InvoiceRepository;
import com.retail.billingservice.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final SalesClient salesClient;
    private final CustomerClient customerClient;
    private final BillingMapper billingMapper;

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

        // Build invoice from sale
        Invoice invoice = Invoice.builder()
                .saleId(sale.getId())
                .customerId(sale.getCustomerId())
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .currency("INR")
                .invoiceStatus(InvoiceStatus.GENERATED)
                .build();

        // Map sale items to invoice items
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            sale.getItems().forEach(saleItem -> {
                InvoiceItem item = InvoiceItem.builder()
                        .productId(saleItem.getProductId())
                        .variantId(saleItem.getVariantId())
                        .sku(saleItem.getSku())
                        .barcode(saleItem.getBarcode())
                        .productName(saleItem.getProductName())
                        .variantName(saleItem.getVariantName())
                        .quantity(saleItem.getQuantity())
                        .unitPrice(saleItem.getUnitPrice())
                        .discountAmount(saleItem.getDiscountAmount())
                        .taxAmount(saleItem.getTaxAmount())
                        .lineTotal(saleItem.getLineTotal())
                        .build();
                invoice.addItem(item);
            });
        }

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);

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
    public List<InvoiceResponse> getAllInvoices() {
        return billingMapper.toInvoiceResponses(invoiceRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomer(Long customerId) {
        return billingMapper.toInvoiceResponses(
                invoiceRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional
    public InvoiceResponse markPaid(Long invoiceId, MarkPaidRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found: " + invoiceId));

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvoiceAlreadyPaidException(
                    "Invoice is already paid: " + invoiceId);
        }

        BigDecimal paidAmount = request.getPaidAmount();
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0
                || paidAmount.compareTo(invoice.getTotal()) > 0) {
            throw new InvalidPaymentException(
                    "Paid amount must be greater than 0 and less than or equal to invoice total (" + invoice.getTotal() + ")");
        }

        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setTransactionReference(request.getTransactionReference());
        invoice.setPaidAmount(paidAmount);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setInvoiceStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());

        Invoice updated = invoiceRepository.save(invoice);
        return billingMapper.toInvoiceResponse(updated);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found: " + invoiceId));

        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        Invoice updated = invoiceRepository.save(invoice);
        return billingMapper.toInvoiceResponse(updated);
    }

    private String generateInvoiceNumber() {
        // TODO: Replace count() + 1 with PostgreSQL sequence for production concurrency safety
        long sequence = invoiceRepository.count() + 1;
        int year = java.time.Year.now().getValue();
        return String.format("INV-%d-%06d", year, sequence);
    }

    private boolean isFinalized(SaleDto sale) {
        // Sale is finalized only when status is COMPLETED
        return sale != null && sale.getSaleStatus() == SaleStatus.COMPLETED;
    }

}
