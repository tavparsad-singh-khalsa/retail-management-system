package com.retail.billingservice.mapper;

import com.retail.billingservice.dto.response.InvoiceItemResponse;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import com.retail.billingservice.entity.InvoiceSettings;
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
                .saleNumber(invoice.getSaleNumber())
                .customerId(invoice.getCustomerId())
                .customerName(invoice.getCustomerName())
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

    public InvoiceSettingsResponse toSettingsResponse(InvoiceSettings settings) {
        if (settings == null) {
            return null;
        }

        return InvoiceSettingsResponse.builder()
                .shopName(settings.getShopName())
                .address(settings.getAddress())
                .phoneNumber(settings.getPhoneNumber())
                .email(settings.getEmail())
                .instagramUrl(settings.getInstagramUrl())
                .googleMapsUrl(settings.getGoogleMapsUrl())
                .googleReviewUrl(settings.getGoogleReviewUrl())
                .invoicePrefix(settings.getInvoicePrefix())
                .invoiceTagline(settings.getInvoiceTagline())
                .footerText(settings.getFooterText())
                .paymentTerms(settings.getPaymentTerms())
                .currency(settings.getCurrency())
                .showShopName(settings.getShowShopName())
                .showAddress(settings.getShowAddress())
                .showPhone(settings.getShowPhone())
                .showEmail(settings.getShowEmail())
                .showInstagram(settings.getShowInstagram())
                .showGoogleMaps(settings.getShowGoogleMaps())
                .showGoogleReview(settings.getShowGoogleReview())
                .showTagline(settings.getShowTagline())
                .showPaymentTerms(settings.getShowPaymentTerms())
                .showFooter(settings.getShowFooter())
                .taxEnabled(settings.getTaxEnabled())
                .taxName(settings.getTaxName())
                .taxRate(settings.getTaxRate())
                .showTax(settings.getShowTax())
                .gstin(settings.getGstin())
                .showGstin(settings.getShowGstin())
                .allowPartialPayment(settings.getAllowPartialPayment())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

}
