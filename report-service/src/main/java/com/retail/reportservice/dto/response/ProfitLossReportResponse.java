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
public class ProfitLossReportResponse {

    private String reportNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private BigDecimal totalCostOfGoodsPurchased;
    private BigDecimal grossProfit;
    private BigDecimal profitMarginPercentage;
    private LocalDateTime generatedAt;
}
