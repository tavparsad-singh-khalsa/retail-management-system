package com.retail.product_service.dto.request;

import com.retail.product_service.enums.AdjustmentType;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CreateStockMovementRequest {

    @NotNull(message = "Inventory ID is required")
    private Long inventoryId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Movement quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Reference type is required")
    private ReferenceType referenceType;

    private String referenceNumber;

    // ⭐ NEW: Only required when movementType == ADJUSTMENT
    private AdjustmentType adjustmentType;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}