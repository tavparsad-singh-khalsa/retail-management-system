package com.retail.product_service.controller;

import com.retail.product_service.dto.response.InventoryResponse;
import com.retail.product_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // 1️⃣ Get all inventory records
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    // 2️⃣ Get specific inventory by its ID
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    // 3️⃣ Get inventory by Product Variant ID (The 1:1 mapped ledger)
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<InventoryResponse> getInventoryByVariant(
            @PathVariable Long variantId) {
        return ResponseEntity.ok(inventoryService.getInventoryByVariant(variantId));
    }

    // 4️⃣ Dashboard Report: Low Stock
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockInventories() {
        return ResponseEntity.ok(inventoryService.getLowStockInventories());
    }

    // 5️⃣ Dashboard Report: Out of Stock
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<InventoryResponse>> getOutOfStockInventories() {
        return ResponseEntity.ok(inventoryService.getOutOfStockInventories());
    }
}