package com.retail.sales_service.dto.integration.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;

    private String categoryName;
    private String brandName;

    private String sku;
    private String barcode;

    private BigDecimal purchasePrice;          // V1 only
    private BigDecimal minimumSellingPrice;
    private BigDecimal sellingPrice;

    private Boolean active;

    private LocalDateTime createdAt;
}