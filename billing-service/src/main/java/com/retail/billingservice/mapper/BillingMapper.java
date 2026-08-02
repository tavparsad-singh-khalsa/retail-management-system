package com.retail.billingservice.mapper;

import com.retail.billingservice.dto.response.InvoiceItemResponse;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingMapper {

    public InvoiceResponse toInvoiceResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        List<InvoiceItemResponse> items = invoice.getItems()
                .stream()
                .map(this::toInvoiceItemResponse)
                .toList();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .saleId(invoice.getSaleId())
                .customerId(invoice.getCustomerId())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .total(invoice.getTotal())
                .currency(invoice.getCurrency())
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

    public InvoiceItemResponse toInvoiceItemResponse(InvoiceItem item) {
        if (item == null) {
            return null;
        }

        return InvoiceItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .sku(item.getSku())
                .barcode(item.getBarcode())
                .productName(item.getProductName())
                .variantName(item.getVariantName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    public List<InvoiceResponse> toInvoiceResponses(List<Invoice> invoices) {
        if (invoices == null) {
            return List.of();
        }

        return invoices.stream()
                .map(this::toInvoiceResponse)
                .toList();
    }

}
