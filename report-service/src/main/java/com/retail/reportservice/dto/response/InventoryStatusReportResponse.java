package com.retail.reportservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStatusReportResponse {

    private String reportNumber;
    private long totalProductsCount;
    private long inStockCount;
    private long lowStockCount;
    private long outOfStockCount;
    private List<LowStockItemSummary> lowStockItems;
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItemSummary {
        private Long productId;
        private String sku;
        private String name;
        private int currentStock;
        private int minStockLevel;
    }
}
