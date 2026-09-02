package com.retail.reportservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalInventoryDto {

    private Long id;
    private Long productVariantId;
    private String sku;
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer availableStock;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderLevel;
    private Boolean isActive;
}
