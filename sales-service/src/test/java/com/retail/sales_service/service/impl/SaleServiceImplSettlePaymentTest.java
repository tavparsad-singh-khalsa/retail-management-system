package com.retail.sales_service.service.impl;

import com.retail.sales_service.client.BillingClient;
import com.retail.sales_service.client.ProductClient;
import com.retail.sales_service.dto.integration.request.InventoryAdjustRequest;
import com.retail.sales_service.dto.integration.request.StockMovementRequest;
import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.response.SaleResponse;
import com.retail.sales_service.entity.Payment;
import com.retail.sales_service.entity.Sale;
import com.retail.sales_service.entity.SaleItem;
import com.retail.sales_service.enums.PaymentMethod;
import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.SaleStatus;
import com.retail.sales_service.exception.InvalidSaleException;
import com.retail.sales_service.exception.PaymentValidationException;
import com.retail.sales_service.exception.SaleNotFoundException;
import com.retail.sales_service.mapper.SaleMapper;
import com.retail.sales_service.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplSettlePaymentTest {

    private static final long SALE_ID = 99L;

    @Mock private SaleRepository saleRepository;
    @Mock private SaleMapper saleMapper;
    @Mock private ProductClient productClient;
    @Mock private BillingClient billingClient;
    @InjectMocks private SaleServiceImpl saleService;

    // ── 1. Full settlement of a partially-paid sale ─────────────────────────
    @Test
    void fullSettlementOfPartiallyPaidSaleFinalizesSale() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        Sale saved = settleAndGet(sale, cash("60.00"));

        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        assertEquals(2, saved.getPayments().size());
        saved.getPayments().forEach(p -> assertEquals(PaymentStatus.PAID, p.getPaymentStatus()));
        assertEquals(new BigDecimal("110.00"), totalPaidRaw(saved));

        // Full settlement needs no invoice settings and never touches billing invoices
        verify(billingClient, never()).getInvoiceSettings();
        verify(billingClient, never()).hasActiveInvoice(any(Long.class));
    }

    // ── 2. Another partial settlement when partial payments are enabled ─────
    @Test
    void secondPartialSettlementReducesRemainingWhenEnabled() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        when(billingClient.getInvoiceSettings()).thenReturn(partialPaymentSettings());

        Sale saved = settleAndGet(sale, cash("30.00"));

        assertEquals(SaleStatus.CREATED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PARTIALLY_PAID, saved.getPaymentStatus());
        assertEquals(2, saved.getPayments().size());
        saved.getPayments().forEach(p -> assertEquals(PaymentStatus.PENDING, p.getPaymentStatus()));
        assertEquals(new BigDecimal("80.00"), totalPaidRaw(saved));
    }

    // ── 3. Partial settlement rejected when disabled ────────────────────────
    @Test
    void partialSettlementRejectedWhenDisabled() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        stubSaleLookup(sale);
        when(billingClient.getInvoiceSettings()).thenReturn(
                InvoiceSettingsResponse.builder()
                        .taxEnabled(false)
                        .taxRate(BigDecimal.ZERO)
                        .build());

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("30.00")));

        verify(saleRepository, never()).save(any(Sale.class));
        assertEquals(1, sale.getPayments().size());
    }

    // ── 4. Amount greater than remaining balance rejected ───────────────────
    @Test
    void amountGreaterThanRemainingBalanceRejected() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        stubSaleLookup(sale);

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("60.01")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── 5. Zero amount rejected ──────────────────────────────────────────────
    @Test
    void zeroAmountRejected() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        stubSaleLookup(sale);

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("0.00")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── 6. Negative amount rejected ─────────────────────────────────────────
    @Test
    void negativeAmountRejected() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        stubSaleLookup(sale);

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("-5.00")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── 7. Non-cash without transaction reference rejected ──────────────────
    @Test
    void nonCashWithoutTransactionReferenceRejected() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        stubSaleLookup(sale);

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, new PaymentRequest(PaymentMethod.UPI, new BigDecimal("60.00"), null, null)));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── 8. Non-cash with transaction reference succeeds ─────────────────────
    @Test
    void nonCashWithTransactionReferenceSucceeds() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        Sale saved = settleAndGet(sale,
                new PaymentRequest(PaymentMethod.UPI, new BigDecimal("60.00"), "UPI-RET-001", null));

        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        Payment settled = saved.getPayments().get(1);
        assertEquals(PaymentMethod.UPI, settled.getPaymentMethod());
        assertEquals("UPI-RET-001", settled.getTransactionReference());
        assertEquals(new BigDecimal("60.00"), settled.getAmount());
        assertEquals(PaymentStatus.PAID, settled.getPaymentStatus());
    }

    // ── 8a. OTHER settlement with reference succeeds ────────────────────────
    @Test
    void otherSettlementWithReferenceSucceeds() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        Sale saved = settleAndGet(sale,
                new PaymentRequest(PaymentMethod.OTHER, new BigDecimal("60.00"), "OTX-RET-001", null));

        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        Payment settled = saved.getPayments().get(1);
        assertEquals(PaymentMethod.OTHER, settled.getPaymentMethod());
        assertEquals("OTX-RET-001", settled.getTransactionReference());
        assertEquals(PaymentStatus.PAID, settled.getPaymentStatus());
    }

    // ── 8b. NET_BANKING settlement with reference succeeds ──────────────────
    @Test
    void netBankingSettlementWithReferenceSucceeds() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        Sale saved = settleAndGet(sale,
                new PaymentRequest(PaymentMethod.NET_BANKING, new BigDecimal("60.00"), "NBX-RET-001", null));

        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        Payment settled = saved.getPayments().get(1);
        assertEquals(PaymentMethod.NET_BANKING, settled.getPaymentMethod());
        assertEquals("NBX-RET-001", settled.getTransactionReference());
        assertEquals(PaymentStatus.PAID, settled.getPaymentStatus());
    }

    // ── 9. CASH succeeds without transaction reference ──────────────────────
    @Test
    void cashSucceedsWithoutTransactionReference() {
        Sale sale = partiallyPaidSale(new BigDecimal("100.00")); // unpaid CREATED sale

        Sale saved = settleAndGet(sale, cash("100.00"));

        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        assertEquals(PaymentMethod.CASH, saved.getPayments().getFirst().getPaymentMethod());
        assertEquals(null, saved.getPayments().getFirst().getTransactionReference());
    }

    // ── 10. Already fully-paid / completed sale rejects settlement ──────────
    @Test
    void alreadyFullyPaidSaleRejectsSettlement() {
        Sale sale = fullyPaidSale(new BigDecimal("110.00"));
        stubSaleLookup(sale);

        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("10.00")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── 11. Settlement never deducts inventory ──────────────────────────────
    @Test
    void settlementDoesNotDeductInventoryAgain() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        settleAndGet(sale, cash("60.00"));

        verify(productClient, never()).deductInventory(any(InventoryAdjustRequest.class));
        verify(productClient, never()).getVariantById(any(Long.class));
        verify(productClient, never()).getInventoryByVariantId(any(Long.class));
        verify(productClient, never()).createStockMovement(any(StockMovementRequest.class));
    }

    // ── 12. Settlement never creates duplicate sale items ───────────────────
    @Test
    void settlementDoesNotCreateDuplicateSaleItems() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        SaleItem item = SaleItem.builder()
                .productVariantId(10L)
                .quantity(1)
                .unitPrice(new BigDecimal("110.00"))
                .build();
        item.setSale(sale);
        sale.getSaleItems().add(item);

        Sale saved = settleAndGet(sale, cash("60.00"));

        assertEquals(1, saved.getSaleItems().size());
        assertEquals(1, sale.getSaleItems().size());
    }

    // ── 13. Multiple partial payments eventually become PAID ────────────────
    @Test
    void multiplePartialPaymentsEventuallyBecomePaid() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        when(billingClient.getInvoiceSettings()).thenReturn(partialPaymentSettings());

        settleAndGet(sale, cash("30.00"));
        assertEquals(PaymentStatus.PARTIALLY_PAID, sale.getPaymentStatus());

        settleAndGet(sale, cash("30.00"));

        assertEquals(SaleStatus.COMPLETED, sale.getSaleStatus());
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());
        assertEquals(3, sale.getPayments().size());
        sale.getPayments().forEach(p -> assertEquals(PaymentStatus.PAID, p.getPaymentStatus()));
    }

    // ── 14. Paid totals and remaining balance are mathematically correct ────
    @Test
    void paidTotalsAndRemainingBalanceAreMathematicallyCorrect() {
        Sale sale = partiallyPaidSale(new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("30.00"));

        Sale saved = settleAndGet(sale, cash("30.00"));

        assertEquals(new BigDecimal("100.00"), totalPaidRaw(saved));
        assertEquals(new BigDecimal("100.00"), saved.getTotalAmount());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());
        assertEquals(3, saved.getPayments().size());
    }

    // ── 15. Retry / duplicate behavior per the actual architecture ──────────
    @Test
    void duplicateRetryAfterFullSettlementIsRejected() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        settleAndGet(sale, cash("60.00"));
        assertEquals(PaymentStatus.PAID, sale.getPaymentStatus());

        // A retry of the now-completed settlement is rejected before any change
        assertThrows(PaymentValidationException.class, () ->
                saleService.settlePayment(SALE_ID, cash("60.00")));
        assertEquals(2, sale.getPayments().size());
    }

    @Test
    void duplicatePartialSettlementIsNotDeduplicatedByArchitecture() {
        // The architecture has no idempotency key, no unique transaction
        // reference, and no payment-level dedup. This test documents that a
        // repeated partial settlement is accepted as a NEW payment as long as
        // amount <= remaining (remaining risk, not hidden).
        Sale sale = partiallyPaidSale(new BigDecimal("200.00"), new BigDecimal("50.00"));
        when(billingClient.getInvoiceSettings()).thenReturn(partialPaymentSettings());

        settleAndGet(sale, cash("30.00"));
        settleAndGet(sale, cash("30.00"));

        assertEquals(3, sale.getPayments().size());
        assertEquals(PaymentStatus.PARTIALLY_PAID, sale.getPaymentStatus());
    }

    // ── 16. Final settlement produces the correct invoice/payment state ─────
    @Test
    void finalSettlementProducesCorrectInvoiceState() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));

        Sale saved = settleAndGet(sale, cash("60.00"));

        // billing-service requires COMPLETED + PAID before an invoice snapshot
        assertEquals(SaleStatus.COMPLETED, saved.getSaleStatus());
        assertEquals(PaymentStatus.PAID, saved.getPaymentStatus());

        // This endpoint never touches invoices itself (no duplicate creation)
        verify(billingClient, never()).hasActiveInvoice(any(Long.class));
        verify(billingClient, never()).getInvoiceSettings();
    }

    // ── Eligibility edges ───────────────────────────────────────────────────
    @Test
    void saleNotFoundThrows() {
        when(saleRepository.findById(SALE_ID)).thenReturn(Optional.empty());

        assertThrows(SaleNotFoundException.class, () ->
                saleService.settlePayment(SALE_ID, cash("10.00")));
    }

    @Test
    void cancelledSaleRejectsSettlement() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        sale.setSaleStatus(SaleStatus.CANCELLED);
        stubSaleLookup(sale);

        assertThrows(InvalidSaleException.class, () ->
                saleService.settlePayment(SALE_ID, cash("60.00")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void returnedSaleRejectsSettlement() {
        Sale sale = partiallyPaidSale(new BigDecimal("110.00"), new BigDecimal("50.00"));
        sale.setSaleStatus(SaleStatus.RETURNED);
        stubSaleLookup(sale);

        assertThrows(InvalidSaleException.class, () ->
                saleService.settlePayment(SALE_ID, cash("60.00")));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private Sale settleAndGet(Sale sale, PaymentRequest payment) {
        when(saleRepository.findById(SALE_ID)).thenReturn(Optional.of(sale));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(SaleResponse.builder().id(SALE_ID).build());

        saleService.settlePayment(SALE_ID, payment);
        return sale;
    }

    private void stubSaleLookup(Sale sale) {
        when(saleRepository.findById(SALE_ID)).thenReturn(Optional.of(sale));
    }

    private PaymentRequest cash(String amount) {
        return new PaymentRequest(PaymentMethod.CASH, new BigDecimal(amount), null, null);
    }

    private InvoiceSettingsResponse partialPaymentSettings() {
        return InvoiceSettingsResponse.builder()
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("10.00"))
                .allowPartialPayment(true)
                .build();
    }

    private Sale partiallyPaidSale(BigDecimal total, BigDecimal... paidAmounts) {
        Sale sale = baseSale(total);
        List<Payment> payments = new ArrayList<>();
        for (BigDecimal amount : paidAmounts) {
            Payment payment = Payment.builder()
                    .amount(amount)
                    .paymentMethod(PaymentMethod.CASH)
                    .paymentStatus(PaymentStatus.PENDING)
                    .active(true)
                    .build();
            payment.setSale(sale);
            payments.add(payment);
        }
        sale.setPayments(payments);
        sale.setSaleStatus(SaleStatus.CREATED);
        sale.setPaymentStatus(paidAmounts.length == 0 ? PaymentStatus.PENDING : PaymentStatus.PARTIALLY_PAID);
        return sale;
    }

    private Sale fullyPaidSale(BigDecimal total) {
        Sale sale = baseSale(total);
        Payment payment = Payment.builder()
                .amount(total)
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(PaymentStatus.PAID)
                .active(true)
                .build();
        payment.setSale(sale);
        sale.setPayments(List.of(payment));
        sale.setPaymentStatus(PaymentStatus.PAID);
        sale.setSaleStatus(SaleStatus.COMPLETED);
        return sale;
    }

    private Sale baseSale(BigDecimal total) {
        return Sale.builder()
                .id(SALE_ID)
                .saleNumber("SAL-2026-000099")
                .subtotal(total)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(total)
                .saleStatus(SaleStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    private BigDecimal totalPaidRaw(Sale sale) {
        return sale.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}