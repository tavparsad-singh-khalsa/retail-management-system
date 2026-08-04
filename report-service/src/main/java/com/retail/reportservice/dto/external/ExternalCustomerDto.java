package com.retail.reportservice.dto.external;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalCustomerDto {

    private Long id;
    private String customerNumber;
    private String name;
    private String email;
    private String phone;
}
