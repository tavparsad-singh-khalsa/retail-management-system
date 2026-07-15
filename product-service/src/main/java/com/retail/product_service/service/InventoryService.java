package com.retail.product_service.service;

import com.retail.product_service.dto.request.UpdateInventoryRequest;
import com.retail.product_service.dto.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse getInventoryByVariant(Long productVariantId);

    List<InventoryResponse> getAllInventories();

    List<InventoryResponse> getLowStockInventories();

    InventoryResponse updateInventory(Long id, UpdateInventoryRequest request);

    void activateInventory(Long id);

    void deactivateInventory(Long id);
}