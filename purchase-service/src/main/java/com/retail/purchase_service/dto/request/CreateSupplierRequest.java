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
public class CreateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Size(max = 20, message = "GST number cannot exceed 20 characters")
    private String gstNumber;

    @Email(message = "Please provide a valid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
}