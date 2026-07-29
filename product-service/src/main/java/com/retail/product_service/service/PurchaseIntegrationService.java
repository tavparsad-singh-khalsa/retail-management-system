package com.retail.product_service.service;

import com.retail.product_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;

public interface PurchaseIntegrationService {
    InventoryOperationResponse receiveInventory(InventoryReceiveRequest request);
}