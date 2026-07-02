package com.retail.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @NotBlank(message = "SKU cannot be blank")
    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String sku;

    // Barcode is optional, so no @NotBlank or nullable = false, but it must be unique if provided
    @Column(unique = true, length = 100)
    private String barcode;

    @NotNull(message = "Purchase price is mandatory")
    @DecimalMin(
            value = "0.01",
            message = "Purchase price must be greater than zero"
    )
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull(message = "Selling price is mandatory")
    @PositiveOrZero(message = "Selling price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull(message = "Minimum selling price is mandatory")
    @PositiveOrZero(message = "Minimum selling price cannot be negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumSellingPrice;

    @NotNull(message = "Product reference is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
