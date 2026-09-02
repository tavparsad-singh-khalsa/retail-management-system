package com.retail.sales_service.service.impl;

import com.retail.sales_service.client.BillingClient;
import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.StockMovementRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.InventoryResponse;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.entity.SaleItem;
import com.retail.sales_service.enums.MovementType;
import com.retail.sales_service.enums.SaleStatus;
import com.retail.sales_service.exception.InvalidSaleException;
import com.retail.sales_service.exception.ProductServiceException;
import com.retail.sales_service.exception.SaleHasActiveInvoiceException;
import com.retail.sales_service.exception.SaleNotFoundException;
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplCancelTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private ProductClient productClient;

    @Mock
    private BillingClient billingClient;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Sale completedSale;
    private Sale cancelledSale;
    private SaleItem saleItem;
    private InventoryResponse inventoryResponse;

    @BeforeEach
    void setUp() {
        // Setup sale item
        saleItem = SaleItem.builder()
                .id(1L)
                .productVariantId(100L)
                .quantity(5)
                .unitPrice(new BigDecimal("100"))
                .totalAmount(new BigDecimal("500"))
                .build();

        // Setup completed sale
        List<SaleItem> items = new ArrayList<>();
        items.add(saleItem);

        completedSale = Sale.builder()
                .id(1L)
                .saleNumber("SAL-2026-000001")
                .totalAmount(new BigDecimal("500"))
                .saleStatus(SaleStatus.COMPLETED)
                .saleItems(items)
                .build();

        // Setup cancelled sale
        cancelledSale = Sale.builder()
                .id(2L)
                .saleNumber("SAL-2026-000002")
                .totalAmount(new BigDecimal("300"))
                .saleStatus(SaleStatus.CANCELLED)
                .saleItems(new ArrayList<>())
                .build();

        // Setup inventory response
        inventoryResponse = InventoryResponse.builder()
                .id(10L)
                .productVariantId(100L)
                .currentStock(50)
                .reservedStock(0)
                .availableStock(50)
                .build();
    }

    @Test
    void cancelSale_CompletedSaleWithoutInvoice_RestoresInventory() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        // When
        saleService.cancelSale(1L);

        // Then
        verify(billingClient).hasActiveInvoice(1L);
        verify(productClient).getInventoryByVariantId(100L);
        verify(productClient).createStockMovement(any(StockMovementRequest.class));
        verify(saleRepository).save(argThat(sale -> sale.getSaleStatus() == SaleStatus.CANCELLED));
    }

    @Test
    void cancelSale_ReturnsStockMovement_WithCorrectType() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        // When
        saleService.cancelSale(1L);

        // Then
        verify(productClient).createStockMovement(argThat(request ->
                request.getMovementType() == MovementType.RETURN &&
                request.getQuantity() == 5 &&
                request.getInventoryId() == 10L &&
                "RETURN-SAL-2026-000001-VAR-100".equals(request.getReferenceNumber())
        ));
    }

    @Test
    void cancelSale_UsesDeterministicReferenceFormat() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        // When
        saleService.cancelSale(1L);

        // Then
        verify(productClient).createStockMovement(argThat(request ->
                "RETURN-SAL-2026-000001-VAR-100".equals(request.getReferenceNumber())
        ));
    }

    @Test
    void cancelSale_MultipleItems_ReceivesDistinctDeterministicReferences() {
        // Given
        SaleItem item2 = SaleItem.builder()
                .id(2L)
                .productVariantId(200L)
                .quantity(3)
                .unitPrice(new BigDecimal("50"))
                .totalAmount(new BigDecimal("150"))
                .build();

        List<SaleItem> items = new ArrayList<>();
        items.add(saleItem);
        items.add(item2);

        Sale multiItemSale = Sale.builder()
                .id(3L)
                .saleNumber("SAL-2026-000003")
                .totalAmount(new BigDecimal("650"))
                .saleStatus(SaleStatus.COMPLETED)
                .saleItems(items)
                .build();

        InventoryResponse inventory2 = InventoryResponse.builder()
                .id(20L)
                .productVariantId(200L)
                .currentStock(30)
                .build();

        when(saleRepository.findById(3L)).thenReturn(Optional.of(multiItemSale));
        when(billingClient.hasActiveInvoice(3L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.getInventoryByVariantId(200L)).thenReturn(inventory2);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        // When
        saleService.cancelSale(3L);

        // Then
        verify(productClient).createStockMovement(argThat(request ->
                "RETURN-SAL-2026-000003-VAR-100".equals(request.getReferenceNumber())
        ));
        verify(productClient).createStockMovement(argThat(request ->
                "RETURN-SAL-2026-000003-VAR-200".equals(request.getReferenceNumber())
        ));
    }

    @Test
    void cancelSale_DuplicateVariantAggregatesIntoSingleMovement() {
        // Same variant appearing as two SaleItem rows must be aggregated so the
        // deterministic reference does not collide and product-service deduplication
        // does not swallow the second line's quantity.
        SaleItem duplicate1 = SaleItem.builder()
                .id(10L)
                .productVariantId(100L)
                .quantity(2)
                .unitPrice(new BigDecimal("100"))
                .totalAmount(new BigDecimal("200"))
                .build();
        SaleItem duplicate2 = SaleItem.builder()
                .id(11L)
                .productVariantId(100L)
                .quantity(3)
                .unitPrice(new BigDecimal("100"))
                .totalAmount(new BigDecimal("300"))
                .build();

        List<SaleItem> items = new ArrayList<>();
        items.add(duplicate1);
        items.add(duplicate2);

        Sale duplicateSale = Sale.builder()
                .id(4L)
                .saleNumber("SAL-2026-000004")
                .totalAmount(new BigDecimal("500"))
                .saleStatus(SaleStatus.COMPLETED)
                .saleItems(items)
                .build();

        when(saleRepository.findById(4L)).thenReturn(Optional.of(duplicateSale));
        when(billingClient.hasActiveInvoice(4L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        saleService.cancelSale(4L);

        // Exactly one inventory lookup and one stock movement for the aggregated variant
        verify(productClient, times(1)).getInventoryByVariantId(100L);
        verify(productClient, times(1)).createStockMovement(argThat(request ->
                "RETURN-SAL-2026-000004-VAR-100".equals(request.getReferenceNumber())
                        && request.getQuantity() == 5
                        && request.getMovementType() == MovementType.RETURN
                        && request.getInventoryId() == 10L
        ));
    }

    @Test
    void cancelSale_RetryProducesSameDeterministicReference() {
        // Simulate the retry gap: product-service succeeded but sales-service failed
        // before persisting CANCELLED. A second cancelSale must generate the same
        // reference so product-service replays instead of restoring twice.
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        saleService.cancelSale(1L);
        // second invocation = retry
        // reset sale status to still COMPLETED (simulating failed first persist)
        completedSale.setSaleStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        saleService.cancelSale(1L);

        // Both calls must use identical reference and quantity
        verify(productClient, times(2)).createStockMovement(argThat(request ->
                "RETURN-SAL-2026-000001-VAR-100".equals(request.getReferenceNumber())
                        && request.getQuantity() == 5
        ));
        verify(productClient, times(2)).getInventoryByVariantId(100L);
    }

    @Test
    void cancelSale_WithActiveInvoice_ThrowsException() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(true);

        assertThrows(SaleHasActiveInvoiceException.class, () -> saleService.cancelSale(1L));

        verify(productClient, never()).createStockMovement(any());
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void cancelSale_AlreadyCancelled_ThrowsException() {
        when(saleRepository.findById(2L)).thenReturn(Optional.of(cancelledSale));

        assertThrows(InvalidSaleException.class, () -> saleService.cancelSale(2L));

        verify(productClient, never()).createStockMovement(any());
    }

    @Test
    void cancelSale_SaleNotFound_ThrowsException() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SaleNotFoundException.class, () -> saleService.cancelSale(99L));
    }

    @Test
    void cancelSale_InventoryRestoreFails_ThrowsException() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(completedSale));
        when(billingClient.hasActiveInvoice(1L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(false).build());

        assertThrows(ProductServiceException.class, () -> saleService.cancelSale(1L));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void cancelSale_MultipleItems_RestoresAllItems() {
        // Given - sale with 2 distinct variants
        SaleItem item2 = SaleItem.builder()
                .id(2L)
                .productVariantId(200L)
                .quantity(3)
                .unitPrice(new BigDecimal("50"))
                .totalAmount(new BigDecimal("150"))
                .build();

        List<SaleItem> items = new ArrayList<>();
        items.add(saleItem);
        items.add(item2);

        Sale multiItemSale = Sale.builder()
                .id(3L)
                .saleNumber("SAL-2026-000003")
                .totalAmount(new BigDecimal("650"))
                .saleStatus(SaleStatus.COMPLETED)
                .saleItems(items)
                .build();

        InventoryResponse inventory2 = InventoryResponse.builder()
                .id(20L)
                .productVariantId(200L)
                .currentStock(30)
                .build();

        when(saleRepository.findById(3L)).thenReturn(Optional.of(multiItemSale));
        when(billingClient.hasActiveInvoice(3L)).thenReturn(false);
        when(productClient.getInventoryByVariantId(100L)).thenReturn(inventoryResponse);
        when(productClient.getInventoryByVariantId(200L)).thenReturn(inventory2);
        when(productClient.createStockMovement(any(StockMovementRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());

        saleService.cancelSale(3L);

        verify(productClient, times(2)).createStockMovement(any(StockMovementRequest.class));
        verify(saleRepository).save(argThat(sale -> sale.getSaleStatus() == SaleStatus.CANCELLED));
    }
}
