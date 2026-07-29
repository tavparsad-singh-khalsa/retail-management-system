package com.retail.purchase_service.dto.response;

import com.retail.purchase_service.enums.PurchaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private String purchaseNumber;
    private Long supplierId;
    
    // Included to save the frontend from making an extra API call
    private String supplierName;
    
    private PurchaseStatus status;
    private LocalDate purchaseDate;
    private String remarks;
    private BigDecimal totalAmount;
    
    // Nested line items
    private List<PurchaseItemResponse> items;
    
    private Boolean isActive;
}