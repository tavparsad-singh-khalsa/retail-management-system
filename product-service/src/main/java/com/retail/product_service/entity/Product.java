package com.retail.product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// import java.util.ArrayList;
// import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    // updatable = false ensures Hibernate will never include this in an UPDATE statement
    @Column(unique = true, updatable = false, length = 50)
    private String productCode;

    @NotBlank(message = "Product name is mandatory")
    @Size(min = 2, max = 150, message = "Product name must be less than 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Product description is mandatory")
    @Size(max = 1000, message = "Product description must be less than 1000 characters")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "hasVariants flag cannot be null")
    @Builder.Default // Ensures Lombok's builder respects this default value
    @Column(nullable = false)
    private Boolean hasVariants = true;

    @NotNull(message = "Category is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull(message = "Brand is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

     @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
     @Builder.Default
     private List<ProductVariant> variants = new ArrayList<>();
}