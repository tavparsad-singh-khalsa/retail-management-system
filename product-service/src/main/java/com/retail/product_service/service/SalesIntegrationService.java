package com.retail.product_service.service;

import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;

public interface SalesIntegrationService {
    InventoryOperationResponse deductInventory(InventoryAdjustRequest request);
}
