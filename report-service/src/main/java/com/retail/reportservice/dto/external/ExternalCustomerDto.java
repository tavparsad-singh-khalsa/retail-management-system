package com.retail.reportservice.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalCustomerDto {

    private Long id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
}
