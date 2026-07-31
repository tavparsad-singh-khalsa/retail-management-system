package com.retail.sales_service.client;

import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;

public interface ProductClient {

    ProductVariantResponse getVariantById(Long variantId);

    InventoryOperationResponse deductInventory(InventoryAdjustRequest request);
}
