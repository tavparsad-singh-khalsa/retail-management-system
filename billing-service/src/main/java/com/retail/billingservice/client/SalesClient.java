package com.retail.billingservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Simple RestClient stub for Sales service. Replace with RestClient integration.
 */
@Component
public class SalesClient {

    public SaleDto getFinalizedSale(Long saleId) {
        // Stub - scaffolding only. Replace with real REST call to Sales Service.
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaleDto {
        private Long id;
        private Long customerId;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal total;
        private List<SaleItemDto> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaleItemDto {
        private Long productId;
        private Long variantId;
        private String sku;
        private String barcode;
        private String productName;
        private String variantName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal lineTotal;
    }
}
