package com.retail.billingservice.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment line carried on a finalized sale. The sales service owns this data;
 * billing only deserializes it to snapshot payment info onto an invoice.
 * Payment method is kept as a raw string because the sales enum (NET_BANKING,
 * WALLET, CHEQUE, OTHER) is wider than the billing PaymentMethod enum.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePaymentDto {
    private Long id;
    private String paymentMethod;
    private BigDecimal amount;
    private String transactionReference;
    private String paymentStatus;
}