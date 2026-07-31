package com.retail.sales_service.dto.integration.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOperationResponse {
    private boolean success;
    private String referenceNumber;
    private Integer processedItems;
    private String message;
    private LocalDateTime timestamp;
    private String transactionId;
}