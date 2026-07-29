package com.retail.product_service.service.impl;

import com.retail.product_service.dto.integration.request.InventoryReceiveItemRequest;
import com.retail.product_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.exception.InventoryNotFoundException;
import com.retail.product_service.exception.InvalidStockMovementException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.service.PurchaseIntegrationService;
import com.retail.product_service.service.StockMovementService;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseIntegrationServiceImpl implements PurchaseIntegrationService {

    private static final String PURCHASE_REMARK = "Purchase received from Purchase Service";

    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    @Transactional
    @Override
    public InventoryOperationResponse receiveInventory(InventoryReceiveRequest request) {
        String transactionId = UUID.randomUUID().toString();
        int processedItems = 0;

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidStockMovementException("Inventory receive request contains no items.");
        }

        log.info("Processing purchase {} with {} items", request.getReferenceNumber(), request.getItems().size());

        try {
            for (InventoryReceiveItemRequest item : request.getItems()) {
                Inventory inventory = inventoryRepository.findByProductVariantId(item.getProductVariantId())
                        .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for variant ID: " + item.getProductVariantId()));

                CreateStockMovementRequest movementRequest = CreateStockMovementRequest.builder()
                        .inventoryId(inventory.getId())
                        .movementType(MovementType.PURCHASE)
                        .quantity(item.getQuantity())
                        .referenceType(ReferenceType.PURCHASE_ORDER)
                        .referenceNumber(request.getReferenceNumber())
                        .remarks(PURCHASE_REMARK)
                        .adjustmentType(null)
                        .build();

                stockMovementService.createStockMovement(movementRequest);
                processedItems++;
            }
        } catch (Exception ex) {
            log.error("Failed to process purchase {}. TransactionId={}", request.getReferenceNumber(), transactionId, ex);
            throw ex;
        }

        log.info("Purchase {} processed successfully. TransactionId={}", request.getReferenceNumber(), transactionId);

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
