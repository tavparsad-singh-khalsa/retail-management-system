package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.dto.response.StockMovementResponse;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.Product;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.entity.StockMovement;
import com.retail.product_service.enums.AdjustmentType;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import com.retail.product_service.exception.InsufficientStockException;
import com.retail.product_service.exception.InvalidStockMovementException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * STEP 19D regression guard: the core ledger semantics used by SALE, PURCHASE,
 * RETURN (restore) and ADJUSTMENT movements must remain exactly as before.
 */
@ExtendWith(MockitoExtension.class)
class StockMovementServiceImplTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private StockMovementRepository stockMovementRepository;

    @InjectMocks private StockMovementServiceImpl stockMovementService;

    @Test
    void saleDeductsCurrentStock() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.createStockMovement(movement(1L, MovementType.SALE, 3, ReferenceType.SALES_INVOICE, "SALE-1", null));

        assertEquals(7, inventory.getCurrentStock());
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertEquals(MovementType.SALE, captor.getValue().getMovementType());
        assertEquals(3, captor.getValue().getQuantity());
    }

    @Test
    void saleWithInsufficientStockThrowsWithoutMutation() {
        Inventory inventory = inventory(2, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class,
                () -> stockMovementService.createStockMovement(movement(1L, MovementType.SALE, 5, ReferenceType.SALES_INVOICE, "SALE-1", null)));

        assertEquals(2, inventory.getCurrentStock());
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void purchaseAddsCurrentStock() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.createStockMovement(movement(1L, MovementType.PURCHASE, 20, ReferenceType.PURCHASE_ORDER, "PO-2026-0001", null));

        assertEquals(30, inventory.getCurrentStock());
    }

    @Test
    void returnAddsCurrentStock() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.createStockMovement(movement(1L, MovementType.RETURN, 4, ReferenceType.SALES_RETURN, "RETURN-1", null));

        assertEquals(14, inventory.getCurrentStock());
    }

    @Test
    void duplicateReturnReplaysWithoutIncreasingStock() {
        Inventory inventory = inventory(10, 0);
        StockMovement existingMovement = StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.RETURN)
                .quantity(4)
                .referenceType(ReferenceType.SALES_RETURN)
                .referenceNumber("RETURN-SAL-2026-000001-VAR-100")
                .build();
        existingMovement.setId(99L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("RETURN-SAL-2026-000001-VAR-100", MovementType.RETURN))
                .thenReturn(List.of(existingMovement));

        StockMovementResponse response = stockMovementService.createStockMovement(
                movement(1L, MovementType.RETURN, 4, ReferenceType.SALES_RETURN, "RETURN-SAL-2026-000001-VAR-100", null));

        assertEquals(10, inventory.getCurrentStock());
        assertEquals(99L, response.getId());
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void adjustmentIncreaseAddsCurrentStock() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.createStockMovement(movement(1L, MovementType.ADJUSTMENT, 6, ReferenceType.MANUAL_ADJUSTMENT, "ADJ-1", AdjustmentType.INCREASE));

        assertEquals(16, inventory.getCurrentStock());
    }

    @Test
    void adjustmentDecreaseDeductsCurrentStock() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        stockMovementService.createStockMovement(movement(1L, MovementType.ADJUSTMENT, 3, ReferenceType.MANUAL_ADJUSTMENT, "ADJ-1", AdjustmentType.DECREASE));

        assertEquals(7, inventory.getCurrentStock());
    }

    @Test
    void adjustmentRequiresAdjustmentType() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InvalidStockMovementException.class,
                () -> stockMovementService.createStockMovement(movement(1L, MovementType.ADJUSTMENT, 3, ReferenceType.MANUAL_ADJUSTMENT, "ADJ-1", null)));

        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void nonAdjustmentMovementRequiresReference() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InvalidStockMovementException.class,
                () -> stockMovementService.createStockMovement(movement(1L, MovementType.PURCHASE, 3, ReferenceType.PURCHASE_ORDER, null, null)));

        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void adjustmentTypeIsRejectedForNonAdjustmentMovements() {
        Inventory inventory = inventory(10, 0);
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        assertThrows(InvalidStockMovementException.class,
                () -> stockMovementService.createStockMovement(movement(1L, MovementType.SALE, 3, ReferenceType.SALES_INVOICE, "SALE-1", AdjustmentType.INCREASE)));

        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    private CreateStockMovementRequest movement(Long inventoryId, MovementType type, int quantity,
                                                ReferenceType referenceType, String referenceNumber,
                                                AdjustmentType adjustmentType) {
        return CreateStockMovementRequest.builder()
                .inventoryId(inventoryId)
                .movementType(type)
                .quantity(quantity)
                .referenceType(referenceType)
                .referenceNumber(referenceNumber)
                .adjustmentType(adjustmentType)
                .build();
    }

    private Inventory inventory(int currentStock, int reservedStock) {
        Product product = Product.builder().name("Test Product").build();
        product.setId(7L);
        ProductVariant variant = ProductVariant.builder().sku("SKU-10").product(product).build();
        variant.setId(10L);
        Inventory inventory = Inventory.builder()
                .currentStock(currentStock)
                .reservedStock(reservedStock)
                .minimumStock(0)
                .reorderLevel(0)
                .productVariant(variant)
                .build();
        inventory.setId(1L);
        return inventory;
    }
}