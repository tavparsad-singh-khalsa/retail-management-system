package com.retail.reportservice.dto.external;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSaleDto {

    private Long id;
    private String saleNumber;
    private Long customerId;
    private String saleStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private List<ExternalSaleItemDto> saleItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExternalSaleItemDto {
        private Long productVariantId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
    }
}
