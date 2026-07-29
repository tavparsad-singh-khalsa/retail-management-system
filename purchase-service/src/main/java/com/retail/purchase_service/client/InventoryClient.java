package com.retail.purchase_service.client;

import com.retail.purchase_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.purchase_service.dto.integration.response.InventoryOperationResponse;

public interface InventoryClient {

    // Changed to respect the domain boundary of the Product Service
    InventoryOperationResponse receiveInventory(InventoryReceiveRequest request);

    // Future methods can be added here easily:
    // InventoryOperationResponse reserveStock(InventoryReserveRequest request);
    // InventoryOperationResponse releaseReservation(String referenceNumber);
}