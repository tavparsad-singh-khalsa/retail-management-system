package com.retail.sales_service.dto.request;

import com.retail.sales_service.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 100)
    private String transactionReference;

    @Size(max = 500)
    private String notes;
}
