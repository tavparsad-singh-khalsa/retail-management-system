package com.retail.reportservice.dto.external;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalInvoiceDto {

    private Long id;
    private String invoiceNumber;
    private Long saleId;
    private Long customerId;
    private BigDecimal total;
    private String invoiceStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
