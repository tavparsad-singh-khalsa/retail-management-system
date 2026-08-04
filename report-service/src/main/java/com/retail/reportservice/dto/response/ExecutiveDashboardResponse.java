package com.retail.reportservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutiveDashboardResponse {

    private String reportNumber;
    private long totalCustomers;
    private long totalProducts;
    private long totalSalesCount;
    private BigDecimal totalRevenue;
    private long totalPurchasesCount;
    private BigDecimal totalPurchaseCost;
    private long pendingInvoicesCount;
    private long pendingNotificationsCount;
    private LocalDateTime generatedAt;
}
