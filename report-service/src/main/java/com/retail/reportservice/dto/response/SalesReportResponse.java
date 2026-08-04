package com.retail.reportservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportResponse {

    private String reportNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalSalesCount;
    private BigDecimal totalRevenue;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal averageOrderValue;
    private List<TopProductSummary> topSellingProducts;
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductSummary {
        private Long productId;
        private String productName;
        private long quantitySold;
        private BigDecimal totalRevenue;
    }
}
