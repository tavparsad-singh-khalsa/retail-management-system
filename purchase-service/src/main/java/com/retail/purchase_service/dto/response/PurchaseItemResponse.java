package com.retail.purchase_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseItemResponse {
    private Long id;
    private Long productVariantId;
    private Integer quantity;
    private BigDecimal purchasePrice;
}