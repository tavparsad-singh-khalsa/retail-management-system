package com.retail.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @NotBlank(message = "Image URL is mandatory")
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @NotNull(message = "isPrimary flag cannot be null")
    @Builder.Default
    @Column(nullable = false)
    private Boolean isPrimary = false;

    @NotNull(message = "Display order cannot be null")
    @PositiveOrZero(message = "Display order must be zero or positive")
    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 1;

    @NotNull(message = "Product reference is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Optional: Can be null if the image applies to all variants globally
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
}