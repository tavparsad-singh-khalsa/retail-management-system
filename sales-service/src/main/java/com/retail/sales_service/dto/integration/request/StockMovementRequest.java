package com.retail.sales_service.dto.integration.request;

import com.retail.sales_service.enums.MovementType;
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
public class StockMovementRequest {

    @NotNull
    private Long inventoryId;

    @NotNull
    private MovementType movementType;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private String referenceNumber;

    private String remarks;
}
