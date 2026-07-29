package com.retail.purchase_service.dto.integration.response;

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
    private int processedItems;
    private String message;
    private LocalDateTime timestamp;
    private String transactionId; // ⭐ Fixed: Added for log correlation across microservices
}