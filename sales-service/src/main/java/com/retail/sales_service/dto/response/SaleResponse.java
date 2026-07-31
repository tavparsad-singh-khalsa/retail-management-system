package com.retail.sales_service.dto.response;

import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {
    private Long id;
    private String saleNumber;
    private Long customerId;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private SaleStatus saleStatus;
    private PaymentStatus paymentStatus;

    private LocalDateTime saleDate;

    private String notes;

    @Builder.Default
    private List<SaleItemResponse> saleItems = new ArrayList<>();

    @Builder.Default
    private List<PaymentResponse> payments = new ArrayList<>();
}
