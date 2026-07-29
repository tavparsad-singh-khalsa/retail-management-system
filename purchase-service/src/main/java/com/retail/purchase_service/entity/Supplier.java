package com.retail.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phone"),
                @UniqueConstraint(columnNames = "gst_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Supplier extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(length = 150, nullable = false)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String contactPerson;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 20)
    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Email
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 500)
    @Column(length = 500)
    private String address;
}