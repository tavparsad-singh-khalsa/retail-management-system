package com.retail.reportservice.dto.external;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalPurchaseDto {

    private Long id;
    private String purchaseNumber;
    private Long supplierId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDate purchaseDate;
}
