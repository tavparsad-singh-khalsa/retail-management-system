package com.retail.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Long productVariantId;
    private String sku;

    private String imageUrl;
    private Boolean isPrimary;
    private Integer displayOrder;
    private Boolean isActive;
}