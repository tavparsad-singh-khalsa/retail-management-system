package com.retail.reportservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReportResponse {

    private String reportNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalPurchasesCount;
    private BigDecimal totalPurchaseCost;
    private BigDecimal averagePurchaseValue;
    private LocalDateTime generatedAt;
}
