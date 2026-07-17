package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.dto.response.StockMovementResponse;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    // 1️⃣ Create a stock movement
    @PostMapping
    public ResponseEntity<StockMovementResponse> createStockMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.createStockMovement(request));
    }

    // 2️⃣ Get Movement by ID
    @GetMapping("/{movementId}")
    public ResponseEntity<StockMovementResponse> getMovementById(
            @PathVariable Long movementId) {
        return ResponseEntity.ok(stockMovementService.getMovementById(movementId));
    }

    // 3️⃣ Get Inventory History (Using plural 'inventories' for RESTful consistency)
    @GetMapping("/inventories/{inventoryId}")
    public ResponseEntity<List<StockMovementResponse>> getMovementHistory(
            @PathVariable Long inventoryId) {
        return ResponseEntity.ok(stockMovementService.getMovementHistory(inventoryId));
    }

    // 4️⃣ Filter by Movement Type
    @GetMapping("/inventories/{inventoryId}/type/{movementType}")
    public ResponseEntity<List<StockMovementResponse>> getMovementHistoryByType(
            @PathVariable Long inventoryId,
            @PathVariable MovementType movementType) {

        // Spring automatically converts the string in the path to the MovementType Enum
        return ResponseEntity.ok(stockMovementService.getMovementHistoryByType(inventoryId, movementType));
    }
}