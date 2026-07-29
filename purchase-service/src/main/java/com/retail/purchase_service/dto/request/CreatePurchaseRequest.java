package com.retail.purchase_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotEmpty(message = "Purchase must contain at least one item")
    @Valid // This ensures validation cascades to the nested items
    private List<PurchaseItemRequest> items;
}