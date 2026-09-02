package com.retail.sales_service.dto.integration.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productVariantId;
    private String sku;
    private String productName;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer availableStock;
    private Integer reorderLevel;
    private Boolean isActive;
}
