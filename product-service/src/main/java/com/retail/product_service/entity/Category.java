package com.retail.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Category name is mandatory")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    private String name;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @OneToMany(
            mappedBy = "parentCategory",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Category> childCategories = new ArrayList<>();

    @OneToMany(
            mappedBy = "category",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}