package com.retail.product_service.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
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
public class UpdateInventoryRequest {

    @PositiveOrZero(message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    @PositiveOrZero(message = "Maximum stock cannot be negative")
    private Integer maximumStock;

    @PositiveOrZero(message = "Reorder level cannot be negative")
    private Integer reorderLevel;
}