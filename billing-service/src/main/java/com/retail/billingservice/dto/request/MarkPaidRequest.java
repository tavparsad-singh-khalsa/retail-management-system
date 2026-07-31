package com.retail.billingservice.dto.request;

import com.retail.billingservice.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
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
public class MarkPaidRequest {

    @NotNull
    private PaymentMethod paymentMethod;

    private String transactionReference;

    @NotNull
    private BigDecimal paidAmount;

}
