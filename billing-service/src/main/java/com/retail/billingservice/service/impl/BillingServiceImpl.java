package com.retail.billingservice.service.impl;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceItemResponse;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import com.retail.billingservice.exception.InvoiceAlreadyExistsException;
import com.retail.billingservice.exception.InvoiceNotFoundException;
import com.retail.billingservice.repository.InvoiceRepository;
import com.retail.billingservice.service.BillingService;
import com.retail.billingservice.client.SalesClient;
import com.retail.billingservice.client.SalesClient.SaleDto;
import com.retail.billingservice.client.CustomerClient;
import com.retail.billingservice.model.InvoiceStatus;
import com.retail.billingservice.model.PaymentMethod;
import com.retail.billingservice.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final SalesClient salesClient;
    private final CustomerClient customerClient;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Long saleId = request.getSaleId();

        invoiceRepository.findBySaleId(saleId).ifPresent(inv -> {
            throw new InvoiceAlreadyExistsException("Invoice already exists for sale: " + saleId);
        });

        SaleDto sale = salesClient.getFinalizedSale(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("Sale not found or not finalized: " + saleId);
        }

        boolean customerExists = customerClient.existsById(sale.getCustomerId());
        if (!customerExists) {
            throw new IllegalArgumentException("Customer not found: " + sale.getCustomerId());
        }

        Invoice invoice = Invoice.builder()
                .saleId(sale.getId())
                .customerId(sale.getCustomerId())
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .total(sale.getTotal())
                .invoiceStatus(InvoiceStatus.GENERATED)
                .build();

        List<InvoiceItem> items = sale.getItems().stream().map(si -> {
            InvoiceItem it = InvoiceItem.builder()
                    .productId(si.getProductId())
                    .variantId(si.getVariantId())
                    .sku(si.getSku())
                    .barcode(si.getBarcode())
                    .productName(si.getProductName())
                    .variantName(si.getVariantName())
                    .quantity(si.getQuantity())
                    .unitPrice(si.getUnitPrice())
                    .lineTotal(si.getLineTotal())
                    .build();
            it.setInvoice(invoice);
            return it;
        }).toList();

        invoice.setItems(items);

        long seq = invoiceRepository.count() + 1;
        invoice.setInvoiceNumber("INV-" + String.format("%06d", seq));

        Invoice saved = invoiceRepository.save(invoice);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found: " + id));
        return toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::toResponse).toList();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceItemResponse> items = invoice.getItems().stream().map(i -> InvoiceItemResponse.builder()
                .id(i.getId())
                .productId(i.getProductId())
                .sku(i.getSku())
                .productName(i.getProductName())
                .variantId(i.getVariantId())
                .variantName(i.getVariantName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .lineTotal(i.getLineTotal())
                .build()).toList();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .saleId(invoice.getSaleId())
                .customerId(invoice.getCustomerId())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .total(invoice.getTotal())
                .invoiceStatus(invoice.getInvoiceStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .paymentStatus(invoice.getPaymentStatus())
                .transactionReference(invoice.getTransactionReference())
                .paidAmount(invoice.getPaidAmount())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .items(items)
                .build();
    }
}
