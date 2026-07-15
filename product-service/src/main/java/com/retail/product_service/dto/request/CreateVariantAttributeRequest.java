package com.retail.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariantAttributeRequest {

    @NotNull(message = "Product Variant ID is required")
    private Long productVariantId;

    @NotNull(message = "Attribute ID is required")
    private Long attributeId;

    @NotNull(message = "Attribute Value ID is required")
    private Long attributeValueId;
}