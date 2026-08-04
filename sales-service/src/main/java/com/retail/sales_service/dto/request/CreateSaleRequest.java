package com.retail.sales_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSaleRequest {

    private Long customerId;

    @NotEmpty
    @Valid
    private List<SaleItemRequest> items;

    @Valid
    @Builder.Default
    private List<PaymentRequest> payments = new ArrayList<>();

    @PositiveOrZero
    private BigDecimal discountAmount;

    @PositiveOrZero
    private BigDecimal taxAmount;

    @Size(max = 1000)
    private String notes;
}
