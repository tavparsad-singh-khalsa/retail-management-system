package com.retail.product_service.controller.integration;

import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.exception.InventoryIdempotencyConflictException;
import com.retail.product_service.service.SalesIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            return ResponseEntity.ok(salesIntegrationService.deductInventory(request));
        } catch (DataIntegrityViolationException ex) {
            // A concurrent duplicate carrying the same operation reference was
            // rejected by the unique SALE-movement index, so nothing was
            // deducted. Surface as a retryable conflict instead of a 500; the
            // next retry lands on the idempotent replay path.
            throw new InventoryIdempotencyConflictException(
                    "Duplicate inventory deduction for reference " + request.getReferenceNumber());
        }
    }
}
