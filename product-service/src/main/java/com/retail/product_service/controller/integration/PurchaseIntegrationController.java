package com.retail.product_service.controller.integration;

import com.retail.product_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.service.PurchaseIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integration/inventory")
@RequiredArgsConstructor
public class PurchaseIntegrationController {

    private final PurchaseIntegrationService purchaseIntegrationService;

    @PostMapping("/receive")
    public ResponseEntity<InventoryOperationResponse> receiveInventory(
            @Valid @RequestBody InventoryReceiveRequest request) {

        InventoryOperationResponse response =
                purchaseIntegrationService.receiveInventory(request);

        return ResponseEntity.ok(response);
    }
}