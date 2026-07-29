package com.retail.purchase_service.dto.integration.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReceiveRequest {
    private String referenceNumber;
    private List<InventoryReceiveItemRequest> items;
}