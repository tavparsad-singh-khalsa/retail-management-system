package com.retail.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttributeResponse {

    private Long id;
    private Long productVariantId;

    private String productName;

    private String sku;

    private Long attributeId;
    private String attributeName;

    private Long attributeValueId;
    private String attributeValue;

    private Boolean isActive;
}