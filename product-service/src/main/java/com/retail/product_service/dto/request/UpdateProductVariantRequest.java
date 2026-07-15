package com.retail.product_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UpdateProductVariantRequest {
        @NotNull(message = "Product Id is required")
        @Positive(message = "Invalid Product selected")
        private Long productId;

        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.0", message = "Purchase price cannot be negative")
        private BigDecimal purchasePrice;

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
        private BigDecimal sellingPrice;

        @NotNull(message = "Minimum Selling price is required")
        @DecimalMin(value = "0.0", message = "Minimum Selling price cannot be negative")
        private BigDecimal minimumSellingPrice;

        @NotNull(message = "Initial stock is required")
        @PositiveOrZero(message = "Initial stock cannot be negative")
        private Integer initialStock;
    }
