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
import com.retail.sales_service.entity.SaleItem;
import com.retail.sales_service.entity.Payment;
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
class SaleServiceImplCreateTest {

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
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(SaleResponse.builder().id(42L).build());
    }

    @Test
    void bankTransferPaymentWithReferenceIsAcceptedAndPreserved() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.BANK_TRANSFER, new BigDecimal("110.00"),
                "BTX-STEP11-001", null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        Payment payment = sale.getPayments().getFirst();
        assertEquals(PaymentMethod.BANK_TRANSFER, payment.getPaymentMethod());
        assertEquals("BTX-STEP11-001", payment.getTransactionReference());
        assertEquals(new BigDecimal("110.00"), payment.getAmount());
    }

    @Test
    void otherPaymentWithReferenceIsAcceptedAndPreserved() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.OTHER, new BigDecimal("110.00"),
                "OTX-STEP16-001", null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        Payment payment = sale.getPayments().getFirst();
        assertEquals(PaymentMethod.OTHER, payment.getPaymentMethod());
        assertEquals("OTX-STEP16-001", payment.getTransactionReference());
        assertEquals(new BigDecimal("110.00"), payment.getAmount());
    }

    @Test
    void netBankingPaymentWithReferenceIsAcceptedAndPreserved() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.NET_BANKING, new BigDecimal("110.00"),
                "NBX-STEP16-001", null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        Payment payment = sale.getPayments().getFirst();
        assertEquals(PaymentMethod.NET_BANKING, payment.getPaymentMethod());
        assertEquals("NBX-STEP16-001", payment.getTransactionReference());
        assertEquals(new BigDecimal("110.00"), payment.getAmount());
    }

    @Test
    void fullPaymentFinalizesSale() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, sale.getPayments().getFirst().getPaymentStatus());
    }

    @Test
    void saleItemSnapshotsProductIdentityFromVariant() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00"), null, null)));

        SaleItem item = capturedSale().getSaleItems().getFirst();
        assertEquals(7L, item.getProductId());
        assertEquals("Bangles", item.getProductName());
        assertEquals("BAN-010", item.getSku());
        assertEquals("890000000010", item.getBarcode());
    }

    @Test
    void partialPaymentCreatesButDoesNotFinalizeSale() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("50.00"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.CREATED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PARTIALLY_PAID, sale.getPaymentStatus());
        assertEquals(PaymentStatus.PENDING, sale.getPayments().getFirst().getPaymentStatus());
    }

    @Test
    void missingPaymentCreatesButDoesNotFinalizeSale() {
        stubSuccessfulSaleDependencies();
        CreateSaleRequest request = request();
        saleService.createSale(request);

        Sale sale = capturedSale();
        assertEquals(SaleStatus.CREATED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PENDING, sale.getPaymentStatus());
    }

    @Test
    void nonCashPaymentRequiresTransactionReferenceBeforeInventoryIsDeducted() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(new BigDecimal("100.00"))
                .build());

        assertThrows(PaymentValidationException.class, () ->
                saleService.createSale(request(new PaymentRequest(PaymentMethod.UPI, new BigDecimal("110.00"), "", null))));

        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void precisionEdgeFullPaymentIsCompleted() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(
                new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00000000000001"), null, null),
                new BigDecimal("10.000000000000002")));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        assertEquals(new BigDecimal("110.00"), sale.getTotalAmount());
        assertEquals(new BigDecimal("110.00"), sale.getPayments().getFirst().getAmount());
    }

    @Test
    void precisionEdgePriceBelowDoubleFullPaymentIsCompleted() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(new BigDecimal("0.57"))
                .build());
        when(billingClient.getInvoiceSettings()).thenReturn(InvoiceSettingsResponse.builder()
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("10.00"))
                .build());
        when(productClient.deductInventory(any(InventoryAdjustRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) sale.setId(42L);
            return sale;
        });
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(SaleResponse.builder().id(42L).build());

        saleService.createSale(request(
                new PaymentRequest(PaymentMethod.CASH, new BigDecimal("0.6269999999999999"), null, null),
                new BigDecimal("0.05699999999999999")));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
    }

    @Test
    void precisionEdgePartialPaymentRemainsPartiallyPaid() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(
                new PaymentRequest(PaymentMethod.CASH, new BigDecimal("0.60"), null, null),
                new BigDecimal("10.00")));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.CREATED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PARTIALLY_PAID, sale.getPaymentStatus());
    }

    @Test
    void overpaymentIsRejected() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(new BigDecimal("100.00"))
                .build());

        assertThrows(PaymentValidationException.class, () ->
                saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.01"), null, null))));

        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void precisionEdgeFloatNoiseIsNotTreatedAsOverpayment() {
        stubSuccessfulSaleDependencies();
        saleService.createSale(request(
                new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00000000000003"), null, null),
                new BigDecimal("10.000000000000002")));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
    }

    @Test
    void partialPaymentRejectedWhenFullPaymentRequired() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(new BigDecimal("100.00"))
                .build());
        when(billingClient.getInvoiceSettings()).thenReturn(InvoiceSettingsResponse.builder()
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("10.00"))
                .build());

        assertThrows(PaymentValidationException.class, () ->
                saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("50.00"), null, null))));

        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
    }

    @Test
    void fullPaymentAcceptedWhenFullPaymentRequired() {
        when(productClient.getVariantById(10L)).thenReturn(ProductVariantResponse.builder()
                .id(10L)
                .sellingPrice(new BigDecimal("100.00"))
                .build());
        when(billingClient.getInvoiceSettings()).thenReturn(InvoiceSettingsResponse.builder()
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("10.00"))
                .build());
        when(productClient.deductInventory(any(InventoryAdjustRequest.class))).thenReturn(
                InventoryOperationResponse.builder().success(true).build());
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) sale.setId(42L);
            return sale;
        });
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(SaleResponse.builder().id(42L).build());

        saleService.createSale(request(new PaymentRequest(PaymentMethod.CASH, new BigDecimal("110.00"), null, null)));

        Sale sale = capturedSale();
        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
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

    private CreateSaleRequest request(PaymentRequest payment, BigDecimal taxAmount) {
        CreateSaleRequest request = request(payment);
        request.setTaxAmount(taxAmount);
        return request;
    }

    private CreateSaleRequest request() {
        return CreateSaleRequest.builder()
                .items(List.of(SaleItemRequest.builder().productVariantId(10L).quantity(1).build()))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .build();
    }
}
