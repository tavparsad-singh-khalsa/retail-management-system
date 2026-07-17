package com.retail.product_service.entity;

import com.retail.product_service.enums.AdjustmentType;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockMovement extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    // Business Rule 2: Quantity is ALWAYS positive.
    // The MovementType determines if it's an addition or deduction.
    @NotNull
    @Positive(message = "Movement quantity must be greater than zero")
    @Column(nullable = false)
    private Integer quantity;

    // Business Rule 3: Traceability back to the originating document.
    // Made optional at the entity level; conditional validation belongs in the service.
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    // Added ReferenceType for explicit categorization of the reference number.
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private ReferenceType referenceType;

    // Added @Size to validate before reaching the database.
    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    private AdjustmentType adjustmentType;
}