package com.retail.sales_service.service.impl;

import com.retail.sales_service.client.BillingClient;
import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.response.InventoryOperationResponse;
import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;
import com.retail.sales_service.dto.integration.response.ProductVariantResponse;
import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.request.SaleItemRequest;
import com.retail.sales_service.dto.response.CreateSaleResult;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.exception.IdempotencyConflictException;
import com.retail.sales_service.exception.InvalidSaleException;
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplIdempotencyTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private ProductClient productClient;
    @Mock private BillingClient billingClient;
    @InjectMocks private SaleServiceImpl saleService;

    private void stubSuccessfulSaleDependencies() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .productId(7L)
                .productName("Bangles")
                .sku("BAN-010")
                .barcode("890000000010")
                .sellingPrice(new BigDecimal("100.00"))
                .build());
        when(billingClient.getInvoiceSettings()).thenReturn(InvoiceSettingsResponse.builder()
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("10.00"))
                .allowPartialPayment(true)
                .build());
        when(productClient.deductInventory(any(InventoryAdjustRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) sale.setId(42L);
            return sale;
        });
        when(saleMapper.toResponse(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            return SaleResponse.builder().id(sale.getId()).saleNumber(sale.getSaleNumber()).build();
        });
    }

    @Test
    void firstRequestWithKeyPersistsKeyAndRequestHash() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-001")).thenReturn(Optional.empty());

        CreateSaleResult result = saleService.createSale(request(), "K-001");

        assertFalse(result.replayed());
        assertNotNull(result.response());
        Sale sale = lastSavedSale();
        assertEquals("K-001", sale.getIdempotencyKey());
        assertNotNull(sale.getIdempotencyRequestHash());
        assertEquals(64, sale.getIdempotencyRequestHash().length());
    }

    @Test
    void retryWithSameKeyAndSamePayloadReplaysWithoutSideEffects() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-001")).thenReturn(Optional.empty());

        CreateSaleResult first = saleService.createSale(request(), "K-001");
        Sale persisted = lastSavedSale();

        when(saleRepository.findByIdempotencyKey("K-001")).thenReturn(Optional.of(persisted));

        CreateSaleResult retry = saleService.createSale(request(), "K-001");

        assertTrue(retry.replayed());
        assertEquals(first.response().getSaleNumber(), retry.response().getSaleNumber());
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void sameKeyWithDifferentPayloadThrowsConflict() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-002")).thenReturn(Optional.empty());

        saleService.createSale(request(), "K-002");
        Sale persisted = lastSavedSale();

        when(saleRepository.findByIdempotencyKey("K-002")).thenReturn(Optional.of(persisted));

        CreateSaleRequest different = request();
        different.setDiscountAmount(new BigDecimal("5.00"));

        assertThrows(IdempotencyConflictException.class,
                () -> saleService.createSale(different, "K-002"));
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void differingOnlyInRequestTaxAmountStillReplays() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-003")).thenReturn(Optional.empty());

        CreateSaleRequest withTax = request();
        withTax.setTaxAmount(new BigDecimal("10.00"));
        saleService.createSale(withTax, "K-003");
        Sale persisted = lastSavedSale();

        when(saleRepository.findByIdempotencyKey("K-003")).thenReturn(Optional.of(persisted));

        CreateSaleRequest withoutTax = request();
        withoutTax.setTaxAmount(new BigDecimal("99.00"));

        CreateSaleResult retry = saleService.createSale(withoutTax, "K-003");

        assertTrue(retry.replayed());
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void blankKeyFallsBackToLegacyPath() {
        stubSuccessfulSaleDependencies();

        CreateSaleResult result = saleService.createSale(request(), "   ");

        assertFalse(result.replayed());
        Sale sale = lastSavedSale();
        assertNull(sale.getIdempotencyKey());
        assertNull(sale.getIdempotencyRequestHash());
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void nullKeyFallsBackToLegacyPath() {
        stubSuccessfulSaleDependencies();

        CreateSaleResult result = saleService.createSale(request(), null);

        assertFalse(result.replayed());
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void overlongKeyIsRejectedBeforeAnySideEffects() {
        String longKey = "K".repeat(65);

        assertThrows(InvalidSaleException.class, () -> saleService.createSale(request(), longKey));
        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void keyWithInvalidCharactersIsRejectedBeforeAnySideEffects() {
        assertThrows(InvalidSaleException.class, () -> saleService.createSale(request(), "bad key!"));
        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void differentKeysCreateIndependentSales() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        CreateSaleResult first = saleService.createSale(request(), "K-A");
        CreateSaleResult second = saleService.createSale(request(), "K-B");

        assertFalse(first.replayed());
        assertFalse(second.replayed());
        verify(productClient, times(2)).deductInventory(any(InventoryAdjustRequest.class));
        verify(saleRepository, times(2)).save(any(Sale.class));
    }

    @Test
    void concurrentDuplicateResolvesToReplayFromPersistedWinner() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-CONCURRENT")).thenReturn(Optional.empty());

        saleService.createSale(request(), "K-CONCURRENT");
        Sale winner = lastSavedSale();

        when(saleRepository.findByIdempotencyKey("K-CONCURRENT")).thenReturn(Optional.empty(), Optional.of(winner));
        when(saleRepository.save(any(Sale.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        CreateSaleResult result = saleService.createSale(request(), "K-CONCURRENT");

        assertTrue(result.replayed());
        assertEquals(winner.getSaleNumber(), result.response().getSaleNumber());
    }

    @Test
    void legacySingleArgCreateStillWorks() {
        stubSuccessfulSaleDependencies();

        SaleResponse response = saleService.createSale(request());

        assertNotNull(response);
        verify(productClient, times(1)).deductInventory(any(InventoryAdjustRequest.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void retryAfterUnpersistedAttemptReusesSameDeductionReference() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-REF")).thenReturn(Optional.empty());

        // First attempt deducts but the sale is never committed (key still
        // unresolvable). The retry re-runs the create path and must send the
        // SAME deduction reference so Product Service replays instead of
        // deducting stock a second time.
        CreateSaleResult first = saleService.createSale(request(), "K-REF");
        CreateSaleResult retry = saleService.createSale(request(), "K-REF");

        assertFalse(first.replayed());
        assertFalse(retry.replayed());
        ArgumentCaptor<InventoryAdjustRequest> captor = ArgumentCaptor.forClass(InventoryAdjustRequest.class);
        verify(productClient, times(2)).deductInventory(captor.capture());
        assertEquals(captor.getAllValues().get(0).getReferenceNumber(),
                captor.getAllValues().get(1).getReferenceNumber());
        assertEquals("SAL-INIT-K-REF", captor.getAllValues().get(0).getReferenceNumber());
    }

    private Sale lastSavedSale() {
        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository, times(1)).save(captor.capture());
        return captor.getAllValues().getFirst();
    }

    private CreateSaleRequest request() {
        return CreateSaleRequest.builder()
                .items(List.of(SaleItemRequest.builder().productVariantId(10L).quantity(1).build()))
                .payments(List.of(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00"), null, null)))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}
