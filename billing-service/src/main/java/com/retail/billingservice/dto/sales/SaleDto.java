package com.retail.billingservice.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDto {
    private Long id;
    private String saleNumber;
    private Long customerId;
    private SaleStatus saleStatus;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private List<SaleItemDto> saleItems;
    private List<SalePaymentDto> payments;
}
