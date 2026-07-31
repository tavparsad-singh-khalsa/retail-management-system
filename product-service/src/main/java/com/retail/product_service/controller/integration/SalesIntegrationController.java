package com.retail.product_service.controller.integration;

import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.service.SalesIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class SalesIntegrationController {

    private final SalesIntegrationService salesIntegrationService;

    @PostMapping("/sale-deduct")
    public ResponseEntity<InventoryOperationResponse> deductInventory(@Valid @RequestBody InventoryAdjustRequest request) {
        return ResponseEntity.ok(salesIntegrationService.deductInventory(request));
    }
}
