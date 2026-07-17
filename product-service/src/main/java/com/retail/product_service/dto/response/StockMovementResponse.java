package com.retail.product_service.dto.response;

import com.retail.product_service.enums.AdjustmentType;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;

    private Long inventoryId;
    private String sku;
    private String productName;

    private MovementType movementType;
    private Integer quantity;

    private ReferenceType referenceType;
    private String referenceNumber;
    private String remarks;

    private LocalDateTime createdAt;
    private Long createdBy;

    private AdjustmentType adjustmentType;
}