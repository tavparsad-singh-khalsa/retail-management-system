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
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.SaleStatus;
import com.retail.sales_service.exception.PaymentValidationException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTaxTest {

    @Mock private SaleRepository saleRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private ProductClient productClient;
    @Mock private BillingClient billingClient;
    @InjectMocks private SaleServiceImpl saleService;

    private void stubSaleDependencies() {
        stubVariant(new BigDecimal("100.00"));
        when(productClient.deductInventory(any(InventoryAdjustRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) sale.setId(42L);
            return sale;
        });
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(SaleResponse.builder().id(42L).build());
    }

    private void stubVariant(BigDecimal sellingPrice) {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(sellingPrice)
                .build());
    }

    private void stubSettings(boolean taxEnabled, String taxRate) {
        when(billingClient.getInvoiceSettings()).thenReturn(InvoiceSettingsResponse.builder()
                .taxEnabled(taxEnabled)
                .taxName("GST")
                .taxRate(new BigDecimal(taxRate))
                .allowPartialPayment(true)
                .build());
    }

    @Test
    void taxDisabledProducesZeroTaxEvenWithConfiguredRateAndRequestedTaxAmount() {
        stubSaleDependencies();
        stubSettings(false, "18.00");
        CreateSaleRequest request = request();
        request.setTaxAmount(new BigDecimal("999.00"));

        saleService.createSale(request);

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("0.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("100.00"), sale.getTotalAmount());
    }

    @Test
    void taxEnabledAppliesConfiguredRate() {
        stubSaleDependencies();
        stubSettings(true, "10.00");
        CreateSaleRequest request = request();
        request.setTaxAmount(new BigDecimal("999.00"));

        saleService.createSale(request);

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("10.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), sale.getTotalAmount());
    }

    @Test
    void taxEnabledAppliesNonDefaultRate() {
        stubSaleDependencies();
        stubSettings(true, "18.00");

        saleService.createSale(request());

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("18.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("118.00"), sale.getTotalAmount());
    }

    @Test
    void taxEnabledWithZeroRateProducesZeroTax() {
        stubSaleDependencies();
        stubSettings(true, "0.00");

        saleService.createSale(request());

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("0.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("100.00"), sale.getTotalAmount());
    }

    @Test
    void taxAppliesToDiscountedTaxableAmount() {
        stubSaleDependencies();
        stubSettings(true, "10.00");
        CreateSaleRequest request = request();
        request.setDiscountAmount(new BigDecimal("20.00"));

        saleService.createSale(request);

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("8.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("88.00"), sale.getTotalAmount());
    }

    @Test
    void taxRoundsToNearestCentUsingHalfUp() {
        stubSaleDependencies();
        stubVariant(new BigDecimal("33.33"));
        stubSettings(true, "18.00");

        saleService.createSale(request());

        // 33.33 * 18 / 100 = 5.9994 -> 6.00 (HALF_UP, not truncated)
        Sale sale = capturedSale();
        assertEquals(new BigDecimal("6.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("39.33"), sale.getTotalAmount());
    }

    @Test
    void taxRoundsFractionalCentsUpForLowPricedItem() {
        stubSaleDependencies();
        stubVariant(new BigDecimal("0.57"));
        stubSettings(true, "10.00");

        saleService.createSale(request());

        // 0.57 * 10 / 100 = 0.057 -> 0.06 (HALF_UP, not 0.05)
        Sale sale = capturedSale();
        assertEquals(new BigDecimal("0.06"), sale.getTaxAmount());
        assertEquals(new BigDecimal("0.63"), sale.getTotalAmount());
    }

    @Test
    void taxEnabledExactFullPaymentFinalizesSale() {
        stubSaleDependencies();
        stubSettings(true, "10.00");

        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
    }

    @Test
    void taxEnabledPrecisionEdgeFullPaymentIsNotOverpayment() {
        stubSaleDependencies();
        stubSettings(true, "10.00");

        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00000000000001"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
    }

    @Test
    void taxEnabledGenuinePartialPaymentRemainsPartiallyPaid() {
        stubSaleDependencies();
        stubSettings(true, "10.00");

        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("50.00"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.CREATED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PARTIALLY_PAID, sale.getPaymentStatus());
    }

    @Test
    void taxEnabledOverpaymentIsRejectedBeforeInventoryDeduction() {
        stubVariant(new BigDecimal("100.00"));
        stubSettings(true, "10.00");

        assertThrows(PaymentValidationException.class, () ->
                saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.01"), null, null))));

        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void existingCustomerAppliesConfiguredTax() {
        stubSaleDependencies();
        stubSettings(true, "10.00");
        CreateSaleRequest request = request();
        request.setCustomerId(7L);

        saleService.createSale(request);

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("10.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), sale.getTotalAmount());
    }

    @Test
    void walkInCustomerAppliesConfiguredTax() {
        stubSaleDependencies();
        stubSettings(true, "10.00");

        saleService.createSale(request());

        Sale sale = capturedSale();
        assertEquals(new BigDecimal("10.00"), sale.getTaxAmount());
        assertEquals(new BigDecimal("110.00"), sale.getTotalAmount());
    }

    private Sale capturedSale() {
        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository, times(1)).save(captor.capture());
        return captor.getAllValues().getFirst();
    }

    private CreateSaleRequest request(PaymentRequest payment) {
        CreateSaleRequest request = request();
        request.setPayments(List.of(payment));
        return request;
    }

    private CreateSaleRequest request() {
        return CreateSaleRequest.builder()
                .items(List.of(SaleItemRequest.builder().productVariantId(10L).quantity(1).build()))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}