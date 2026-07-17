package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.dto.response.StockMovementResponse;
import com.retail.product_service.enums.MovementType;

import java.util.List;

public interface StockMovementService {

    StockMovementResponse createStockMovement(CreateStockMovementRequest request);

    StockMovementResponse getMovementById(Long movementId);

    List<StockMovementResponse> getMovementHistory(Long inventoryId);

    List<StockMovementResponse> getMovementHistoryByType(Long inventoryId, MovementType movementType);

}