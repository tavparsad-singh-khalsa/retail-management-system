package com.retail.product_service.service.impl;

import com.retail.product_service.dto.integration.request.InventoryAdjustItemRequest;
import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.StockMovement;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import com.retail.product_service.exception.InventoryIdempotencyConflictException;
import com.retail.product_service.exception.InventoryNotFoundException;
import com.retail.product_service.exception.InvalidStockMovementException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.StockMovementRepository;
import com.retail.product_service.service.SalesIntegrationService;
import com.retail.product_service.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesIntegrationServiceImpl implements SalesIntegrationService {

    private static final String SALE_REMARK = "Sale deducted from Sales Service";

    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    @Override
    public InventoryOperationResponse deductInventory(InventoryAdjustRequest request) {
        String transactionId = UUID.randomUUID().toString();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidStockMovementException("Inventory adjust request contains no items.");
        }

        // Normalize one operation identity: quantities for the same variant are
        // merged so exactly one SALE movement is recorded per (reference, variant).
        // Total deduction stays identical while duplicate request lines can no
        // longer create duplicate rows for the same operation.
        Map<Long, Integer> requestedByVariant = aggregateItems(request);

        log.info("Processing sale deduction {} with {} item(s)",
                request.getReferenceNumber(), requestedByVariant.size());

        try {
            return processOrReplay(request, requestedByVariant, transactionId);
        } catch (Exception ex) {
            log.error("Failed to process sale deduction {}. TransactionId={}",
                    request.getReferenceNumber(), transactionId, ex);
            throw ex;
        }
    }

    private InventoryOperationResponse processOrReplay(InventoryAdjustRequest request,
                                                       Map<Long, Integer> requestedByVariant,
                                                       String transactionId) {
        List<StockMovement> existing = stockMovementRepository
                .findByReferenceNumberAndMovementType(request.getReferenceNumber(), MovementType.SALE);

        if (!existing.isEmpty()) {
            // This reference was already handled. Only an identical operation may
            // be replayed: same variants with the same quantities. Anything else
            // is a conflict and must not touch stock.
            replayOrReject(request, requestedByVariant, existing);
            log.info("Sale deduction {} replayed idempotently (no stock change). TransactionId={}",
                    request.getReferenceNumber(), transactionId);
            return successResponse(request, requestedByVariant.size(), "Inventory already updated (idempotent replay).", transactionId);
        }

        int processedItems = 0;
        for (Map.Entry<Long, Integer> entry : requestedByVariant.entrySet()) {
            Long variantId = entry.getKey();
            int quantity = entry.getValue();

            Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                    .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for variant ID: " + variantId));

            CreateStockMovementRequest movementRequest = CreateStockMovementRequest.builder()
                    .inventoryId(inventory.getId())
                    .movementType(MovementType.SALE)
                    .quantity(quantity)
                    .referenceType(ReferenceType.SALES_INVOICE)
                    .referenceNumber(request.getReferenceNumber())
                    .remarks(SALE_REMARK)
                    .adjustmentType(null)
                    .build();

            stockMovementService.createStockMovement(movementRequest);
            processedItems++;
        }

        return successResponse(request, processedItems, "Inventory updated successfully.", transactionId);
    }

    private void replayOrReject(InventoryAdjustRequest request,
                                Map<Long, Integer> requestedByVariant,
                                List<StockMovement> existing) {
        Map<Long, Integer> existingByVariant = new LinkedHashMap<>();
        for (StockMovement movement : existing) {
            if (movement.getReferenceType() != ReferenceType.SALES_INVOICE) {
                throw new InventoryIdempotencyConflictException(
                        "Reference " + request.getReferenceNumber()
                                + " was already used for a different inventory operation type");
            }
            existingByVariant.merge(movement.getInventory().getProductVariant().getId(),
                    movement.getQuantity(), Integer::sum);
        }

        if (existingByVariant.size() != requestedByVariant.size()) {
            throw conflict(request.getReferenceNumber());
        }
        for (Map.Entry<Long, Integer> entry : requestedByVariant.entrySet()) {
            Integer existingQuantity = existingByVariant.get(entry.getKey());
            if (existingQuantity == null || !Objects.equals(existingQuantity, entry.getValue())) {
                throw conflict(request.getReferenceNumber());
            }
        }
    }

    private InventoryIdempotencyConflictException conflict(String referenceNumber) {
        return new InventoryIdempotencyConflictException(
                "Reference " + referenceNumber
                        + " was already used for a different inventory deduction");
    }

    private Map<Long, Integer> aggregateItems(InventoryAdjustRequest request) {
        Map<Long, Integer> aggregated = new LinkedHashMap<>();
        for (InventoryAdjustItemRequest item : request.getItems()) {
            aggregated.merge(item.getProductVariantId(), item.getQuantity(), Integer::sum);
        }
        return aggregated;
    }

    private InventoryOperationResponse successResponse(InventoryAdjustRequest request,
                                                       int processedItems,
                                                       String message,
                                                       String transactionId) {
        return InventoryOperationResponse.builder()
                .success(true)
                .referenceNumber(request.getReferenceNumber())
                .processedItems(processedItems)
                .message(message)
                .timestamp(LocalDateTime.now())
                .transactionId(transactionId)
                .build();
    }
}