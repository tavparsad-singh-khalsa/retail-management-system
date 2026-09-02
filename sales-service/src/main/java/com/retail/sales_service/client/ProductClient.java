package com.retail.sales_service.client;

import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.request.StockMovementRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.InventoryResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;

public interface ProductClient {

    ProductVariantResponse getVariantById(Long variantId);

    InventoryResponse getInventoryByVariantId(Long variantId);

    InventoryOperationResponse deductInventory(InventoryAdjustRequest request);

    InventoryOperationResponse createStockMovement(StockMovementRequest request);
}
