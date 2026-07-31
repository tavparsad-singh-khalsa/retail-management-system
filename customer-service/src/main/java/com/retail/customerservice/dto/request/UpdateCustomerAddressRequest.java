package com.retail.customerservice.dto.request;

import com.retail.customerservice.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class UpdateCustomerAddressRequest {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must be at most 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be at most 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[A-Za-z0-9\\- ]{3,20}$", message = "Postal code must be 3 to 20 alphanumeric characters")
    private String postalCode;

    @NotNull(message = "Address type is required")
    private AddressType addressType;

    @NotNull(message = "Default address flag is required")
    private Boolean defaultAddress;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
