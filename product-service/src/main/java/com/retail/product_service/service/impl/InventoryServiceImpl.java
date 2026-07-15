package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.UpdateInventoryRequest;
import com.retail.product_service.dto.response.InventoryResponse;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.ProductVariantRepository;
import com.retail.product_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;

    // ==========================================
    // CORE SERVICE METHODS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVariant(Long productVariantId) {
        ProductVariant variant = getActiveVariantOrThrow(productVariantId);

        Inventory inventory = inventoryRepository.findByProductVariant(variant)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for variant ID: " + productVariantId));

        // Prevent leaking inactive inventories
        if (!inventory.getIsActive()) {
            throw new InventoryInactiveException("Inventory configuration is currently inactive.");
        }

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        // Only return active inventories to the client
        return inventoryRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockInventories() {
        // ERP Report - Filter active inventories where current stock is at or below the reorder level
        return inventoryRepository.findByIsActiveTrue().stream()
                .filter(inv -> inv.getCurrentStock() <= inv.getReorderLevel())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, UpdateInventoryRequest request) {
        // Only update configuration thresholds, NEVER current/reserved stock
        Inventory inventory = getActiveInventoryOrThrow(id);

        // Determine new values (fallback to existing if not provided in the request)
        Integer newMinimumStock = request.getMinimumStock() != null ? request.getMinimumStock() : inventory.getMinimumStock();
        Integer newMaximumStock = request.getMaximumStock() != null ? request.getMaximumStock() : inventory.getMaximumStock();
        Integer newReorderLevel = request.getReorderLevel() != null ? request.getReorderLevel() : inventory.getReorderLevel();

        // Validate the configuration before applying
        validateInventoryConfiguration(newMinimumStock, newMaximumStock, newReorderLevel);

        inventory.setMinimumStock(newMinimumStock);
        inventory.setMaximumStock(newMaximumStock);
        inventory.setReorderLevel(newReorderLevel);

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public void activateInventory(Long id) {
        Inventory inventory = getInventoryOrThrow(id);

        if (inventory.getIsActive()) return;

        // Ensure parent hierarchy is completely active before allowing activation
        getActiveVariantOrThrow(inventory.getProductVariant().getId());

        inventory.setIsActive(true);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void deactivateInventory(Long id) {
        Inventory inventory = getInventoryOrThrow(id);

        if (!inventory.getIsActive()) return;

        inventory.setIsActive(false);
        inventoryRepository.save(inventory);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private Inventory getInventoryOrThrow(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + id));
    }

    private Inventory getActiveInventoryOrThrow(Long id) {
        Inventory inventory = getInventoryOrThrow(id);
        if (!inventory.getIsActive()) {
            throw new InventoryInactiveException("Inventory configuration is currently inactive.");
        }
        return inventory;
    }

    private ProductVariant getActiveVariantOrThrow(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with ID: " + id));

        // Deep hierarchy validation
        if (!variant.getIsActive()) {
            throw new ProductInactiveException("Cannot use an inactive variant.");
        }
        if (!variant.getProduct().getIsActive()) {
            throw new ProductInactiveException("Cannot use variant: Parent product is inactive.");
        }
        if (!variant.getProduct().getCategory().getIsActive()) {
            throw new CategoryInactiveException("Cannot use variant: Parent category is inactive.");
        }
        if (!variant.getProduct().getBrand().getIsActive()) {
            throw new BrandInactiveException("Cannot use variant: Parent brand is inactive.");
        }

        return variant;
    }

    private void validateInventoryConfiguration(Integer minStock, Integer maxStock, Integer reorderLevel) {
        // minimumStock <= reorderLevel
        if (minStock != null && reorderLevel != null && minStock > reorderLevel) {
            throw new InvalidInventoryConfigurationException("Minimum stock cannot be greater than the reorder level.");
        }

        // reorderLevel <= maximumStock (if maximumStock is set)
        if (reorderLevel != null && maxStock != null && reorderLevel > maxStock) {
            throw new InvalidInventoryConfigurationException("Reorder level cannot be greater than the maximum stock.");
        }

        // Transitive fallback: minimumStock <= maximumStock
        if (minStock != null && maxStock != null && minStock > maxStock) {
            throw new InvalidInventoryConfigurationException("Minimum stock cannot be greater than the maximum stock.");
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productVariantId(inventory.getProductVariant().getId())
                .sku(inventory.getProductVariant().getSku())
                .productId(inventory.getProductVariant().getProduct().getId())
                .productName(inventory.getProductVariant().getProduct().getName())
                .currentStock(inventory.getCurrentStock())
                .reservedStock(inventory.getReservedStock())
                // Dynamically calculated field (Current - Reserved)
                .availableStock(inventory.getCurrentStock() - inventory.getReservedStock())
                .minimumStock(inventory.getMinimumStock())
                .maximumStock(inventory.getMaximumStock())
                .reorderLevel(inventory.getReorderLevel())
                .isActive(inventory.getIsActive())
                .build();
    }
}