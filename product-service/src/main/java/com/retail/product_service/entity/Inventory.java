package com.retail.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Inventory extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false, unique = true)
    private ProductVariant productVariant;

    @NotNull
    @PositiveOrZero(message = "Current stock cannot be negative")
    @Column(name = "current_stock", nullable = false)
    @Builder.Default
    private Integer currentStock = 0;

    @NotNull
    @PositiveOrZero(message = "Reserved stock cannot be negative")
    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    @NotNull
    @PositiveOrZero(message = "Minimum stock cannot be negative")
    @Column(name = "minimum_stock", nullable = false)
    @Builder.Default
    private Integer minimumStock = 0;

    // IMPROVEMENT: Removed @NotNull and nullable = false to allow dynamic warehouse capacity
    @PositiveOrZero(message = "Maximum stock cannot be negative")
    @Column(name = "maximum_stock")
    private Integer maximumStock;

    @NotNull
    @PositiveOrZero(message = "Reorder level cannot be negative")
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 0;

    public Integer getAvailableStock() {
        return currentStock - reservedStock;
    }
}