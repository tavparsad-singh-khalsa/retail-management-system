package com.retail.sales_service.dto.integration.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustItemRequest {

    @NotNull
    private Long productVariantId;

    @NotNull
    @Positive
    private Integer quantity;
}