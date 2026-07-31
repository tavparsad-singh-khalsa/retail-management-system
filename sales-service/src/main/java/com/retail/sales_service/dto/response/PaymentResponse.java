package com.retail.sales_service.dto.response;

import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String transactionReference;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}
