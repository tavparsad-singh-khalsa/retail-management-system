package com.retail.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "brands")
public class Brand extends BaseEntity {

    @NotBlank(message = "Brand name is mandatory")
    @Size(max = 100, message = "Brand name must be less than 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

     @Builder.Default
     @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
     private List<Product> products = new ArrayList<>();
}
