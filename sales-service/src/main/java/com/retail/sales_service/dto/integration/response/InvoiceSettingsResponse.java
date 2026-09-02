package com.retail.sales_service.dto.integration.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettingsResponse {

    private Boolean taxEnabled;
    private String taxName;
    private BigDecimal taxRate;
    private Boolean allowPartialPayment;
}