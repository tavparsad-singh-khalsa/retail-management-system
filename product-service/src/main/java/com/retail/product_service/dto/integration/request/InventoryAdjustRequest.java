package com.retail.product_service.dto.integration.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustRequest {

    @NotBlank(message = "Reference number cannot be blank")
    private String referenceNumber;

    @NotEmpty(message = "At least one inventory item is required")
    @Valid
    private List<InventoryAdjustItemRequest> items;
}
