package com.retail.sales_service.dto.integration.request;

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
    @NotBlank
    private String referenceNumber;

    @NotEmpty
    @Valid
    private List<InventoryAdjustItemRequest> items;
}