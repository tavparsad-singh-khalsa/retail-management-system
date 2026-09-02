package com.retail.product_service.service.impl;

import com.retail.product_service.dto.integration.request.InventoryAdjustItemRequest;
import com.retail.product_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.product_service.dto.integration.response.InventoryOperationResponse;
import com.retail.product_service.dto.request.CreateStockMovementRequest;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.entity.StockMovement;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import com.retail.product_service.exception.InsufficientStockException;
import com.retail.product_service.exception.InventoryIdempotencyConflictException;
import com.retail.product_service.exception.InventoryNotFoundException;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.StockMovementRepository;
import com.retail.product_service.service.SalesIntegrationService;
import com.retail.product_service.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * STEP 19D: a repeated inventory deduction carrying the same operation reference
 * must never deduct stock twice. Same reference + identical operation replays
 * idempotently; same reference + different operation is a conflict; different
 * references are independent.
 */
@ExtendWith(MockitoExtension.class)
class SalesIntegrationServiceImplIdempotencyTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private StockMovementService stockMovementService;
    @Mock private StockMovementRepository stockMovementRepository;

    @InjectMocks private SalesIntegrationServiceImpl salesIntegrationService;

    @Test
    void firstDeductionCreatesOneSaleMovementAndSucceeds() {
        Inventory inventory = inventory(1L, variant(10L));
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventory));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());

        InventoryOperationResponse response = salesIntegrationService.deductInventory(request("REF-1", 10L, 5));

        assertTrue(response.isSuccess());
        assertEquals("REF-1", response.getReferenceNumber());
        assertEquals(1, response.getProcessedItems());

        ArgumentCaptor<CreateStockMovementRequest> captor = ArgumentCaptor.forClass(CreateStockMovementRequest.class);
        verify(stockMovementService).createStockMovement(captor.capture());
        CreateStockMovementRequest movement = captor.getValue();
        assertEquals(1L, movement.getInventoryId());
        assertEquals(MovementType.SALE, movement.getMovementType());
        assertEquals(5, movement.getQuantity());
        assertEquals(ReferenceType.SALES_INVOICE, movement.getReferenceType());
        assertEquals("REF-1", movement.getReferenceNumber());
    }

    @Test
    void sameReferenceSamePayloadReplaysWithoutDeductingTwice() {
        StockMovement existing = saleMovement(inventory(1L, variant(10L)), "REF-1", 5);
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        InventoryOperationResponse response = salesIntegrationService.deductInventory(request("REF-1", 10L, 5));

        assertTrue(response.isSuccess());
        assertEquals("REF-1", response.getReferenceNumber());
        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void sameReferenceDoesNotCreateASecondStockMovement() {
        StockMovement existing = saleMovement(inventory(1L, variant(10L)), "REF-1", 5);
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        salesIntegrationService.deductInventory(request("REF-1", 10L, 5));

        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void sameReferenceDifferentQuantityIsRejected() {
        StockMovement existing = saleMovement(inventory(1L, variant(10L)), "REF-1", 5);
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        assertThrows(InventoryIdempotencyConflictException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 4)));
        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
    }

    @Test
    void sameReferenceDifferentVariantIsRejected() {
        StockMovement existing = saleMovement(inventory(1L, variant(10L)), "REF-1", 5);
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        assertThrows(InventoryIdempotencyConflictException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 5, 20L, 2)));
        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
    }

    @Test
    void sameReferenceDifferentMovementTypeSemanticsIsRejected() {
        StockMovement existing = StockMovement.builder()
                .inventory(inventory(1L, variant(10L)))
                .movementType(MovementType.SALE)
                .quantity(5)
                .referenceType(ReferenceType.MANUAL_ADJUSTMENT)
                .referenceNumber("REF-1")
                .build();
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        assertThrows(InventoryIdempotencyConflictException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 5)));
        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
    }

    @Test
    void differentReferencePerformsIndependentDeduction() {
        Inventory inventoryA = inventory(1L, variant(10L));
        Inventory inventoryB = inventory(2L, variant(20L));
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventoryA));
        when(inventoryRepository.findByProductVariantId(20L)).thenReturn(java.util.Optional.of(inventoryB));
        when(stockMovementRepository.findByReferenceNumberAndMovementType(any(), eq(MovementType.SALE)))
                .thenReturn(List.of());

        salesIntegrationService.deductInventory(request("REF-A", 10L, 2));
        salesIntegrationService.deductInventory(request("REF-B", 20L, 3));

        verify(stockMovementService, times(2)).createStockMovement(any(CreateStockMovementRequest.class));
    }

    @Test
    void multiItemSaleCreatesOneMovementPerVariant() {
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventory(1L, variant(10L))));
        when(inventoryRepository.findByProductVariantId(20L)).thenReturn(java.util.Optional.of(inventory(2L, variant(20L))));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());

        InventoryOperationResponse response = salesIntegrationService.deductInventory(request("REF-1", 10L, 2, 20L, 3));

        assertTrue(response.isSuccess());
        assertEquals(2, response.getProcessedItems());
        ArgumentCaptor<CreateStockMovementRequest> captor = ArgumentCaptor.forClass(CreateStockMovementRequest.class);
        verify(stockMovementService, times(2)).createStockMovement(captor.capture());
        List<Integer> quantities = captor.getAllValues().stream().map(CreateStockMovementRequest::getQuantity).toList();
        assertTrue(quantities.contains(2) && quantities.contains(3));
    }

    @Test
    void duplicateVariantLinesInOneRequestAreAggregatedIntoOneMovement() {
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventory(1L, variant(10L))));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());

        InventoryAdjustItemRequest first = item(10L, 2);
        InventoryAdjustItemRequest second = item(10L, 3);
        InventoryAdjustRequest request = InventoryAdjustRequest.builder()
                .referenceNumber("REF-1")
                .items(List.of(first, second))
                .build();

        InventoryOperationResponse response = salesIntegrationService.deductInventory(request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getProcessedItems());
        ArgumentCaptor<CreateStockMovementRequest> captor = ArgumentCaptor.forClass(CreateStockMovementRequest.class);
        verify(stockMovementService, times(1)).createStockMovement(captor.capture());
        assertEquals(5, captor.getValue().getQuantity());
    }

    @Test
    void retryWithReaggregatedEquivalentPositionReplays() {
        StockMovement existing = saleMovement(inventory(1L, variant(10L)), "REF-1", 5);
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of(existing));

        InventoryAdjustItemRequest first = item(10L, 2);
        InventoryAdjustItemRequest second = item(10L, 3);
        InventoryAdjustRequest request = InventoryAdjustRequest.builder()
                .referenceNumber("REF-1")
                .items(List.of(first, second))
                .build();

        InventoryOperationResponse response = salesIntegrationService.deductInventory(request);

        assertTrue(response.isSuccess());
        verify(stockMovementService, never()).createStockMovement(any(CreateStockMovementRequest.class));
    }

    @Test
    void insufficientStockStillFailsTheWholeRequest() {
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventory(1L, variant(10L))));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());
        when(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient current stock for SALE"));

        assertThrows(InsufficientStockException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 99)));
    }

    @Test
    void missingInventoryStillFailsTheRequest() {
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 1)));
    }

    @Test
    void dataIntegrityViolationFromRacePropagatesForConflictMapping() {
        // Concurrent duplicate: the pre-check saw no existing movements, but the
        // unique SALE-movement index rejected the second insert. The service must
        // propagate the constraint violation so the controller maps it to a
        // retryable conflict — never a second deduction.
        when(inventoryRepository.findByProductVariantId(10L)).thenReturn(java.util.Optional.of(inventory(1L, variant(10L))));
        when(stockMovementRepository.findByReferenceNumberAndMovementType("REF-1", MovementType.SALE))
                .thenReturn(List.of());
        when(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThrows(DataIntegrityViolationException.class,
                () -> salesIntegrationService.deductInventory(request("REF-1", 10L, 5)));
    }

    private InventoryAdjustRequest request(String reference, Long variantId, int quantity) {
        return InventoryAdjustRequest.builder()
                .referenceNumber(reference)
                .items(List.of(item(variantId, quantity)))
                .build();
    }

    private InventoryAdjustRequest request(String reference, Long variantIdA, int quantityA, Long variantIdB, int quantityB) {
        return InventoryAdjustRequest.builder()
                .referenceNumber(reference)
                .items(List.of(item(variantIdA, quantityA), item(variantIdB, quantityB)))
                .build();
    }

    private InventoryAdjustItemRequest item(Long variantId, int quantity) {
        return InventoryAdjustItemRequest.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private StockMovement saleMovement(Inventory inventory, String reference, int quantity) {
        return StockMovement.builder()
                .inventory(inventory)
                .movementType(MovementType.SALE)
                .quantity(quantity)
                .referenceType(ReferenceType.SALES_INVOICE)
                .referenceNumber(reference)
                .build();
    }

    private Inventory inventory(Long id, ProductVariant variant) {
        Inventory inventory = Inventory.builder().currentStock(100).productVariant(variant).build();
        inventory.setId(id);
        return inventory;
    }

    private ProductVariant variant(Long id) {
        ProductVariant variant = ProductVariant.builder().sku("SKU-" + id).build();
        variant.setId(id);
        return variant;
    }
}