package com.retail.sales_service.mapper;

import org.springframework.stereotype.Component;
import com.retail.sales_service.dto.response.PaymentResponse;
import com.retail.sales_service.dto.response.SaleItemResponse;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Payment;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.entity.SaleItem;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SaleMapper {

    public SaleResponse toResponse(Sale s) {
        List<SaleItemResponse> itemResponses = s.getSaleItems() == null ? List.of() : s.getSaleItems().stream().map(this::toItemResponse).collect(Collectors.toList());
        List<PaymentResponse> paymentResponses = s.getPayments() == null ? List.of() : s.getPayments().stream().map(this::toPaymentResponse).collect(Collectors.toList());

        return SaleResponse.builder()
                .id(s.getId())
                .saleNumber(s.getSaleNumber())
                .customerId(s.getCustomerId())
                .subtotal(s.getSubtotal())
                .discountAmount(s.getDiscountAmount())
                .taxAmount(s.getTaxAmount())
                .totalAmount(s.getTotalAmount())
                .saleStatus(s.getSaleStatus())
                .paymentStatus(s.getPaymentStatus())
                .saleDate(s.getSaleDate())
                .notes(s.getNotes())
                .saleItems(itemResponses)
                .payments(paymentResponses)
                .build();
    }

    private SaleItemResponse toItemResponse(SaleItem i) {
        return SaleItemResponse.builder()
                .id(i.getId())
                .productVariantId(i.getProductVariantId())
                .productId(i.getProductId())
                .sku(i.getSku())
                .barcode(i.getBarcode())
                .productName(i.getProductName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .discountAmount(i.getDiscountAmount())
                .taxAmount(i.getTaxAmount())
                .totalAmount(i.getTotalAmount())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .paymentMethod(p.getPaymentMethod())
                .amount(p.getAmount())
                .transactionReference(p.getTransactionReference())
                .paymentStatus(p.getPaymentStatus())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}
