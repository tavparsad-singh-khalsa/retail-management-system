package com.retail.customerservice.dto.response;

import com.retail.customerservice.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddressResponse {

    private Long id;

    private Long customerId;

    private String customerCode;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private AddressType addressType;

    private Boolean defaultAddress;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
