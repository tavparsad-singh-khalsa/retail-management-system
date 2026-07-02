package com.retail.product_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(
        name = "attribute_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "attribute_id",
                                "value"
                        }
                )
        }
)
public class AttributeValue extends BaseEntity {

    @NotBlank(message = "Value cannot be blank")
    @Size(max = 100, message = "Value must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String value;

    @NotNull(message = "Attribute is mandatory")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;
}