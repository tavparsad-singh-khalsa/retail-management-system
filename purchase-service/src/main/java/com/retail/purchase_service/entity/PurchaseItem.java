package com.retail.purchase_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseItem extends BaseEntity {

    // The owning side of the bidirectional relationship
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    // External reference to the Product Service (Microservice Boundary)
    @Positive(message = "Product variant ID must be positive")
    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Positive(message = "Quantity must be greater than zero")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Standardized money handling for the unit price
    @DecimalMin(value = "0.01", message = "Purchase price must be at least 0.01")
    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePrice;

}