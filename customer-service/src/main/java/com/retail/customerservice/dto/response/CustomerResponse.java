package com.retail.customerservice.dto.response;

import com.retail.customerservice.enums.CustomerStatus;
import com.retail.customerservice.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;

    private String customerCode;

    private String firstName;

    private String lastName;

    private String mobileNumber;

    private String email;

    private Gender gender;

    private LocalDate dateOfBirth;

    private LocalDate anniversary;

    private CustomerStatus customerStatus;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    private List<CustomerAddressResponse> addresses = new ArrayList<>();
}
