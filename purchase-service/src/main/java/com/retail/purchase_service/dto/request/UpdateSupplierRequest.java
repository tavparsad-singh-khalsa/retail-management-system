package com.retail.purchase_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String gstNumber;

    @Email(message = "Please provide a valid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 500)
    private String address;
}