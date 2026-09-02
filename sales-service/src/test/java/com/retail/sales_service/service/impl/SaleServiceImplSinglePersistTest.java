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
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * STEP 19C focused tests: a successful create-sale must persist the sale
 * exactly once and assign the generated sale number on that same persisted
 * entity (and in the response) so the committed row never carries a NULL
 * sale_number.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceImplSinglePersistTest {

    private static final Pattern SALE_NUMBER = Pattern.compile("SAL-\\d{4}-000042");

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
    void createSalePersistsExactlyOnce() {
        stubSuccessfulSaleDependencies();

        saleService.createSale(request());

        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    void generatedSaleNumberIsAssignedOnThePersistedEntity() {
        stubSuccessfulSaleDependencies();

        saleService.createSale(request());

        Sale saved = singleSavedSale();
        assertNotNull(saved.getSaleNumber());
        assertTrue(SALE_NUMBER.matcher(saved.getSaleNumber()).matches(),
                "unexpected sale number: " + saved.getSaleNumber());
    }

    @Test
    void generatedSaleNumberAppearsInTheResponse() {
        stubSuccessfulSaleDependencies();

        SaleResponse response = saleService.createSale(request());

        assertNotNull(response.getSaleNumber());
        assertEquals(singleSavedSale().getSaleNumber(), response.getSaleNumber());
    }

    @Test
    void replayReturnsThePersistedSaleNumber() {
        stubSuccessfulSaleDependencies();
        when(saleRepository.findByIdempotencyKey("K-S19")).thenReturn(Optional.empty());

        CreateSaleResult first = saleService.createSale(request(), "K-S19");
        Sale persisted = singleSavedSale();

        when(saleRepository.findByIdempotencyKey("K-S19")).thenReturn(Optional.of(persisted));

        CreateSaleResult retry = saleService.createSale(request(), "K-S19");

        assertTrue(retry.replayed());
        assertNotNull(first.response().getSaleNumber());
        assertEquals(persisted.getSaleNumber(), retry.response().getSaleNumber());
    }

    private Sale singleSavedSale() {
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