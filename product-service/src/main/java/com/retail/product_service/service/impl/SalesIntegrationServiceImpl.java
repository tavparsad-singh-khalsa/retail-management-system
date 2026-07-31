package com.retail.product_service.service.impl;

import com.retail.product_service.dto.integration.request.InventoryAdjustItemRequest;
import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import com.retail.product_service.exception.InventoryNotFoundException;
import com.retail.product_service.exception.InvalidStockMovementException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.service.SalesIntegrationService;
import com.retail.product_service.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesIntegrationServiceImpl implements SalesIntegrationService {

    private static final String SALE_REMARK = "Sale deducted from Sales Service";

    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    @Transactional
    @Override
    public InventoryOperationResponse deductInventory(InventoryAdjustRequest request) {
        String transactionId = UUID.randomUUID().toString();
        int processedItems = 0;

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidStockMovementException("Inventory adjust request contains no items.");
        }

        log.info("Processing sale deduction {} with {} items", request.getReferenceNumber(), request.getItems().size());

        try {
            for (InventoryAdjustItemRequest item : request.getItems()) {
                Inventory inventory = inventoryRepository.findByProductVariantId(item.getProductVariantId())
                        .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for variant ID: " + item.getProductVariantId()));

                CreateStockMovementRequest movementRequest = CreateStockMovementRequest.builder()
                        .inventoryId(inventory.getId())
                        .movementType(MovementType.SALE)
                        .quantity(item.getQuantity())
                        .referenceType(ReferenceType.SALES_INVOICE)
                        .referenceNumber(request.getReferenceNumber())
                        .remarks(SALE_REMARK)
                        .adjustmentType(null)
                        .build();

                stockMovementService.createStockMovement(movementRequest);
                processedItems++;
            }
        } catch (Exception ex) {
            log.error("Failed to process sale deduction {}. TransactionId={}", request.getReferenceNumber(), transactionId, ex);
            throw ex;
        }

        log.info("Sale deduction {} processed successfully. TransactionId={}", request.getReferenceNumber(), transactionId);

        return InventoryOperationResponse.builder()
                .success(true)
                .referenceNumber(request.getReferenceNumber())
                .processedItems(processedItems)
                .message("Inventory updated successfully.")
                .timestamp(LocalDateTime.now())
                .transactionId(transactionId)
                .build();
    }
}
