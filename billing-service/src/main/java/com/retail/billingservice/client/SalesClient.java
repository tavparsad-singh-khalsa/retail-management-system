package com.retail.billingservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public interface SalesClient {

    SaleDto getFinalizedSale(Long saleId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class SaleDto {
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
    class SaleItemDto {
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

