package com.retail.product_service.entity;

import com.retail.product_service.enums.AttributeDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "attributes")
public class Attribute extends BaseEntity {

    @NotBlank(message = "Attribute name is mandatory")
    @Size(max = 100, message = "Attribute name must be less than 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Attribute description is mandatory")
    @Size(max = 500, message = "Attribute description must be less than 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Data type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttributeDataType dataType;

    @NotNull(message = "Required flag cannot be null")
    @Builder.Default
    @Column(nullable = false)
    private Boolean required = false;

     @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
     @Builder.Default
     private List<AttributeValue> attributeValues = new ArrayList<>();
}