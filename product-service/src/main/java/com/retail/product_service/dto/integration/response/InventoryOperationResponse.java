package com.retail.product_service.dto.integration.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> errors;
}