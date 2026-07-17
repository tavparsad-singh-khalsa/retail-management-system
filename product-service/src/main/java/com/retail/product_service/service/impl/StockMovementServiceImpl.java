package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.dto.response.StockMovementResponse;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.StockMovement;
import com.retail.product_service.enums.AdjustmentType;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.exception.InsufficientStockException;
import com.retail.product_service.exception.InvalidStockMovementException;
import com.retail.product_service.exception.InventoryNotFoundException;
import com.retail.product_service.exception.StockMovementNotFoundException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.StockMovementRepository;
import com.retail.product_service.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    // ==========================================
    // CORE SERVICE METHODS
    // ==========================================

    @Transactional
    @Override
    public StockMovementResponse createStockMovement(CreateStockMovementRequest request) {
        // 1. Fetch
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + request.getInventoryId()));

        // 2. Validate
        validateReference(request);
        validateStockAvailability(inventory, request);

        // 3. Mutate State
        updateInventory(inventory, request);

        // 4. Map to Entity
        StockMovement movement = mapToEntity(request, inventory);

        // 5. Save (Inventory is persisted via dirty checking or explicit save)
        inventoryRepository.save(inventory);
        StockMovement savedMovement = stockMovementRepository.save(movement);

        // 6. Respond
        return mapToResponse(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse getMovementById(Long movementId) {
        StockMovement movement = stockMovementRepository.findById(movementId)
                .orElseThrow(() -> new StockMovementNotFoundException("Stock movement not found with ID: " + movementId));
        return mapToResponse(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementHistory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + inventoryId));

        return stockMovementRepository.findByInventoryOrderByCreatedAtDesc(inventory).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementHistoryByType(Long inventoryId, MovementType movementType) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + inventoryId));

        return stockMovementRepository.findByInventoryAndMovementType(inventory, movementType).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private void validateReference(CreateStockMovementRequest request) {
        MovementType movementType = request.getMovementType();
        String referenceNumber = request.getReferenceNumber();
        AdjustmentType adjustmentType = request.getAdjustmentType();

        if (movementType == MovementType.ADJUSTMENT) {
            if (adjustmentType == null) {
                throw new InvalidStockMovementException(
                        "Adjustment type is required for ADJUSTMENT movements."
                );
            }
        } else {
            if (adjustmentType != null) {
                throw new InvalidStockMovementException(
                        "Adjustment type is only allowed for ADJUSTMENT movements."
                );
            }
            if (!StringUtils.hasText(referenceNumber)) {
                throw new InvalidStockMovementException(
                        "Reference number is required for " + movementType + " movements."
                );
            }
        }
    }

    private void validateStockAvailability(Inventory inventory, CreateStockMovementRequest request) {
        MovementType movementType = request.getMovementType();
        Integer quantity = request.getQuantity();

        int currentStock = inventory.getCurrentStock();
        int reservedStock = inventory.getReservedStock();
        int availableStock = inventory.getAvailableStock();

        switch (movementType) {
            case SALE:
            case DAMAGE:
                if (currentStock < quantity) {
                    throw new InsufficientStockException(
                            "Insufficient current stock for " + movementType + ". Current: "
                                    + currentStock + ", Requested: " + quantity
                    );
                }
                break;
            case RESERVE:
                if (availableStock < quantity) {
                    throw new InsufficientStockException(
                            "Insufficient available stock to reserve. Available: "
                                    + availableStock + ", Requested: " + quantity
                    );
                }
                break;
            case UNRESERVE:
                if (reservedStock < quantity) {
                    throw new InsufficientStockException(
                            "Cannot unreserve more stock than is currently reserved. Reserved: "
                                    + reservedStock + ", Requested: " + quantity
                    );
                }
                break;
            case ADJUSTMENT:
                if (request.getAdjustmentType() == AdjustmentType.DECREASE && currentStock < quantity) {
                    throw new InsufficientStockException(
                            "Insufficient current stock for ADJUSTMENT (DECREASE). Current: "
                                    + currentStock + ", Requested: " + quantity
                    );
                }
                break;
            case PURCHASE:
            case RETURN:
            default:
                // No checks needed for additions
                break;
        }
    }

    private void updateInventory(Inventory inventory, CreateStockMovementRequest request) {
        MovementType movementType = request.getMovementType();
        Integer quantity = request.getQuantity();

        int currentStock = inventory.getCurrentStock();
        int reservedStock = inventory.getReservedStock();

        switch (movementType) {
            case PURCHASE:
            case RETURN:
                inventory.setCurrentStock(currentStock + quantity);
                break;
            case SALE:
            case DAMAGE:
                inventory.setCurrentStock(currentStock - quantity);
                break;
            case RESERVE:
                inventory.setReservedStock(reservedStock + quantity);
                break;
            case UNRESERVE:
                inventory.setReservedStock(reservedStock - quantity);
                break;
            case ADJUSTMENT:
                if (request.getAdjustmentType() == AdjustmentType.INCREASE) {
                    inventory.setCurrentStock(currentStock + quantity);
                } else if (request.getAdjustmentType() == AdjustmentType.DECREASE) {
                    inventory.setCurrentStock(currentStock - quantity);
                }
                break;
        }
    }

    private StockMovement mapToEntity(CreateStockMovementRequest request, Inventory inventory) {
        return StockMovement.builder()
                .inventory(inventory)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .referenceType(request.getReferenceType())
                .referenceNumber(request.getReferenceNumber())
                .adjustmentType(request.getAdjustmentType())
                .remarks(request.getRemarks())
                // BaseEntity audit fields (createdAt, createdBy) handled automatically by JPA Auditing
                .build();
    }

    private StockMovementResponse mapToResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .inventoryId(movement.getInventory().getId())
                .sku(movement.getInventory().getProductVariant().getSku())
                .productName(movement.getInventory().getProductVariant().getProduct().getName())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .referenceType(movement.getReferenceType())
                .referenceNumber(movement.getReferenceNumber())
                // Included the adjustment type so the UI knows if it was an INCREASE/DECREASE
                .adjustmentType(movement.getAdjustmentType())
                .remarks(movement.getRemarks())
                .createdAt(movement.getCreatedAt())
                .createdBy(movement.getCreatedBy())
                .build();
    }
}