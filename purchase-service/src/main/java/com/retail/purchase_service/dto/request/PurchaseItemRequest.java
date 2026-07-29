package com.retail.purchase_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemRequest {

    @NotNull(message = "Product variant ID is required")
    @Positive(message = "Product variant ID must be positive")
    private Long productVariantId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.01", message = "Purchase price must be at least 0.01")
    private BigDecimal purchasePrice;
}