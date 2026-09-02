package com.retail.billingservice.service.impl;

import com.retail.billingservice.client.CustomerClient;
import com.retail.billingservice.client.SalesClient;
import com.retail.billingservice.dto.customer.CustomerDto;
import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.dto.sales.SaleDto;
import com.retail.billingservice.dto.sales.SaleItemDto;
import com.retail.billingservice.dto.sales.SalePaymentDto;
import com.retail.billingservice.dto.sales.SaleStatus;
import com.retail.billingservice.entity.Invoice;
import com.retail.billingservice.entity.InvoiceItem;
import com.retail.billingservice.exception.CustomerNotFoundException;
import com.retail.billingservice.exception.InvoiceAlreadyExistsException;
import com.retail.billingservice.exception.SaleNotFinalizedException;
import com.retail.billingservice.mapper.BillingMapper;
import com.retail.billingservice.model.InvoiceStatus;
import com.retail.billingservice.model.PaymentMethod;
import com.retail.billingservice.model.PaymentStatus;
import com.retail.billingservice.repository.InvoiceRepository;
import com.retail.billingservice.service.InvoiceSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private SalesClient salesClient;
    @Mock private CustomerClient customerClient;
    @Mock private BillingMapper billingMapper;
    @Mock private InvoiceSettingsService invoiceSettingsService;
    @InjectMocks private BillingServiceImpl billingService;

    private final List<Invoice> savedInvoices = new ArrayList<>();

    @BeforeEach
    void setUp() {
        savedInvoices.clear();
    }

    @Test
    void rejectsInvoiceForNonFinalizedSale() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(sale(SaleStatus.CREATED));

        assertThrows(SaleNotFinalizedException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createsInvoiceForCompletedSale() {
        stubHappyPath(defaultSettings(), 1L);

        assertDoesNotThrow(() -> billingService.createInvoice(new CreateInvoiceRequest(42L)));

        verify(invoiceRepository).save(any());
        assertEquals(1, savedInvoices.size());
    }

    // 1) Default prefix generates INV-...
    @Test
    void generatesInvoiceWithDefaultPrefixWhenSettingsUseDefaults() {
        Invoice saved = createInvoiceWithSettings(defaultSettings(), 1L);

        assertEquals(expectedInvoiceNumber("INV", 1L), saved.getInvoiceNumber());
    }

    // 2) Custom prefix generates the configured prefix
    @Test
    void generatesInvoiceWithConfiguredPrefix() {
        Invoice saved = createInvoiceWithSettings(settings("AC", "INR"), 7L);

        assertEquals(expectedInvoiceNumber("AC", 7L), saved.getInvoiceNumber());
    }

    // 3) Changing the prefix affects only newly generated invoices
    @Test
    void changingPrefixAppliesOnlyToNewlyGeneratedInvoices() {
        Invoice first = createInvoiceWithSettings(settings("INV", "INR"), 10L);
        Invoice second = createInvoiceWithSettings(settings("AC", "INR"), 11L);

        assertEquals(expectedInvoiceNumber("INV", 10L), first.getInvoiceNumber());
        assertEquals(expectedInvoiceNumber("AC", 11L), second.getInvoiceNumber());
        assertEquals(2, savedInvoices.size());
    }

    // 4) Existing invoice numbers are never restated or changed
    @Test
    void preStoredInvoiceIsNeverSavedOrChangedByNewInvoiceCreation() {
        Invoice existing = Invoice.builder()
                .id(99L)
                .invoiceNumber(expectedInvoiceNumber("INV", 1L))
                .saleId(7L)
                .currency("INR")
                .build();

        Invoice created = createInvoiceWithSettings(defaultSettings(), 2L);

        assertEquals(expectedInvoiceNumber("INV", 1L), existing.getInvoiceNumber());
        assertEquals(expectedInvoiceNumber("INV", 2L), created.getInvoiceNumber());
        assertNotEquals(existing.getInvoiceNumber(), created.getInvoiceNumber());
        assertEquals(1, savedInvoices.size());
        verify(invoiceRepository, never()).save(existing);
    }

    // 5) Blank/null prefix falls back to the safe default
    @Test
    void blankPrefixFallsBackToDefault() {
        Invoice saved = createInvoiceWithSettings(settings("   ", "INR"), 1L);

        assertEquals(expectedInvoiceNumber("INV", 1L), saved.getInvoiceNumber());
    }

    @Test
    void nullPrefixFallsBackToDefault() {
        Invoice saved = createInvoiceWithSettings(settings(null, "INR"), 3L);

        assertEquals(expectedInvoiceNumber("INV", 3L), saved.getInvoiceNumber());
    }

    // A prefix entered with its separator still renders a single separator
    @Test
    void prefixEnteredWithTrailingDashStillRendersSingleSeparator() {
        Invoice saved = createInvoiceWithSettings(settings("AC-", "INR"), 8L);

        assertEquals(expectedInvoiceNumber("AC", 8L), saved.getInvoiceNumber());
    }

    // 6) Default currency is used
    @Test
    void usesDefaultCurrencyFromSettings() {
        Invoice saved = createInvoiceWithSettings(defaultSettings(), 1L);

        assertEquals("INR", saved.getCurrency());
    }

    // 7) Custom currency is stored on newly generated invoices
    @Test
    void storesConfiguredCurrencyOnNewInvoice() {
        Invoice saved = createInvoiceWithSettings(settings("INV", "USD"), 1L);

        assertEquals("USD", saved.getCurrency());
    }

    // 8) Changing the currency affects only newly generated invoices
    @Test
    void changingCurrencyAppliesOnlyToNewlyGeneratedInvoices() {
        Invoice first = createInvoiceWithSettings(settings("INV", "INR"), 1L);
        Invoice second = createInvoiceWithSettings(settings("INV", "USD"), 2L);

        assertEquals("INR", first.getCurrency());
        assertEquals("USD", second.getCurrency());
        assertEquals(2, savedInvoices.size());
    }

    // 9) Currency configuration never changes monetary values
    @Test
    void currencyConfigurationDoesNotAlterMonetaryValues() {
        Invoice saved = createInvoiceWithSettings(settings("AC", "USD"), 1L);

        assertEquals(0, saved.getSubtotal().compareTo(new BigDecimal("100.00")));
        assertEquals(0, saved.getDiscountAmount().compareTo(BigDecimal.ZERO));
        assertEquals(0, saved.getTotal().compareTo(new BigDecimal("110.00")));
    }

    // 10) Tax amount is copied unchanged from the sale
    @Test
    void taxAmountCopiedUnchangedFromSale() {
        Invoice saved = createInvoiceWithSettings(defaultSettings(), 1L);

        assertEquals(0, saved.getTaxAmount().compareTo(new BigDecimal("10.00")));
    }

    // 13) Invoice retry: a transient failure does not block a later retry
    @Test
    void retryAfterTransientFailureCreatesInvoiceExactlyOnce() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(sale(SaleStatus.COMPLETED));
        when(customerClient.existsById(9L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));
        verify(invoiceRepository, never()).save(any());

        stubHappyPath(defaultSettings(), 5L);
        assertDoesNotThrow(() -> billingService.createInvoice(new CreateInvoiceRequest(42L)));

        verify(invoiceRepository, times(1)).save(any());
        assertEquals(1, savedInvoices.size());
    }

    // A repeated creation attempt for the same sale is rejected (no duplicates)
    @Test
    void creatingInvoiceForTheSameSaleTwiceThrowsAlreadyExists() {
        when(invoiceRepository.findBySaleId(42L))
                .thenReturn(Optional.of(Invoice.builder().id(1L).build()));

        assertThrows(InvoiceAlreadyExistsException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));

        verify(invoiceRepository, never()).save(any());
        verify(invoiceRepository, never()).nextInvoiceNumberSequence();
    }

    // 14) Invoice numbers are sequential and never duplicated
    @Test
    void generatesSequentialUniqueInvoiceNumbers() {
        stubHappyPath(defaultSettings(), 1L, 2L);

        billingService.createInvoice(new CreateInvoiceRequest(42L));
        billingService.createInvoice(new CreateInvoiceRequest(42L));

        assertEquals(2, savedInvoices.size());
        assertEquals(expectedInvoiceNumber("INV", 1L), savedInvoices.get(0).getInvoiceNumber());
        assertEquals(expectedInvoiceNumber("INV", 2L), savedInvoices.get(1).getInvoiceNumber());
        assertNotEquals(savedInvoices.get(0).getInvoiceNumber(), savedInvoices.get(1).getInvoiceNumber());
        verify(invoiceRepository, times(2)).nextInvoiceNumberSequence();
    }

    // 15) Product identity from the sale is carried into invoice items
    @Test
    void invoiceItemsCarryProductIdentityFromSale() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .saleItems(List.of(SaleItemDto.builder()
                        .id(1L)
                        .productVariantId(10L)
                        .productId(7L)
                        .sku("BAN-010")
                        .barcode("890000000010")
                        .productName("Bangles")
                        .quantity(1)
                        .unitPrice(new BigDecimal("100.00"))
                        .discountAmount(BigDecimal.ZERO)
                        .taxAmount(new BigDecimal("10.00"))
                        .totalAmount(new BigDecimal("110.00"))
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        assertDoesNotThrow(() -> billingService.createInvoice(new CreateInvoiceRequest(42L)));

        InvoiceItem item = savedInvoices.get(0).getItems().getFirst();
        assertEquals(7L, item.getProductId());
        assertEquals(10L, item.getVariantId());
        assertEquals("BAN-010", item.getSku());
        assertEquals("890000000010", item.getBarcode());
        assertEquals("Bangles", item.getProductName());
    }

    // 16) Fully paid sales snapshot sale number, customer name, and payment info
    @Test
    void fullyPaidSaleSnapshotsReferenceCustomerAndPayment() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("UPI")
                        .amount(new BigDecimal("110.00"))
                        .transactionReference("UTR-123456789012")
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(customerClient.getCustomer(9L)).thenReturn(CustomerDto.builder()
                .id(9L).firstName("Ravi").lastName("Kumar").build());
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        billingService.createInvoice(new CreateInvoiceRequest(42L));

        Invoice invoice = savedInvoices.get(0);
        assertEquals("SAL-2026-000042", invoice.getSaleNumber());
        assertEquals("Ravi Kumar", invoice.getCustomerName());
        assertEquals(0, invoice.getPaidAmount().compareTo(new BigDecimal("110.00")));
        assertEquals(PaymentMethod.UPI, invoice.getPaymentMethod());
        assertEquals("UTR-123456789012", invoice.getTransactionReference());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        assertEquals(InvoiceStatus.PAID, invoice.getInvoiceStatus());
    }

    // 16a) An OTHER payment is preserved on the invoice instead of becoming N/A
    @Test
    void otherPaymentIsPreservedOnInvoice() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("OTHER")
                        .amount(new BigDecimal("110.00"))
                        .transactionReference("OTX-STEP16-001")
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(customerClient.getCustomer(9L)).thenReturn(CustomerDto.builder()
                .id(9L).firstName("Ravi").lastName("Kumar").build());
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        billingService.createInvoice(new CreateInvoiceRequest(42L));

        Invoice invoice = savedInvoices.get(0);
        assertEquals(PaymentMethod.OTHER, invoice.getPaymentMethod());
        assertEquals("OTX-STEP16-001", invoice.getTransactionReference());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
    }

    // 16b) NET_BANKING is intentionally collapsed onto BANK_TRANSFER
    @Test
    void netBankingIsMappedToBankTransferOnInvoice() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("NET_BANKING")
                        .amount(new BigDecimal("110.00"))
                        .transactionReference("NBX-STEP16-001")
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(customerClient.getCustomer(9L)).thenReturn(CustomerDto.builder()
                .id(9L).firstName("Ravi").lastName("Kumar").build());
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        billingService.createInvoice(new CreateInvoiceRequest(42L));

        Invoice invoice = savedInvoices.get(0);
        assertEquals(PaymentMethod.BANK_TRANSFER, invoice.getPaymentMethod());
        assertEquals("NBX-STEP16-001", invoice.getTransactionReference());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
    }

    // 16c) WALLET payment is mapped to PaymentMethod.WALLET
    @Test
    void walletPaymentIsPreservedOnInvoice() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("WALLET")
                        .amount(new BigDecimal("110.00"))
                        .transactionReference("WAL-STEP16-001")
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(customerClient.getCustomer(9L)).thenReturn(CustomerDto.builder()
                .id(9L).firstName("Ravi").lastName("Kumar").build());
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        billingService.createInvoice(new CreateInvoiceRequest(42L));

        Invoice invoice = savedInvoices.get(0);
        assertEquals(PaymentMethod.WALLET, invoice.getPaymentMethod());
        assertEquals("WAL-STEP16-001", invoice.getTransactionReference());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
    }

    // 16d) CHEQUE payment is mapped to PaymentMethod.CHEQUE
    @Test
    void chequePaymentIsPreservedOnInvoice() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("CHEQUE")
                        .amount(new BigDecimal("110.00"))
                        .transactionReference("CHQ-STEP16-001")
                        .build()))
                .build());
        when(customerClient.existsById(9L)).thenReturn(true);
        when(customerClient.getCustomer(9L)).thenReturn(CustomerDto.builder()
                .id(9L).firstName("Ravi").lastName("Kumar").build());
        when(invoiceSettingsService.getSettings()).thenReturn(defaultSettings());
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(1L);
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());

        billingService.createInvoice(new CreateInvoiceRequest(42L));

        Invoice invoice = savedInvoices.get(0);
        assertEquals(PaymentMethod.CHEQUE, invoice.getPaymentMethod());
        assertEquals("CHQ-STEP16-001", invoice.getTransactionReference());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
    }

    // 17) A COMPLETED sale that is only partially paid is never finalized for invoicing
    @Test
    void completedSaleWithPartialPaymentIsNotFinalized() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PARTIALLY_PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of(SalePaymentDto.builder()
                        .paymentMethod("CASH")
                        .amount(new BigDecimal("40.00"))
                        .build()))
                .build());

        assertThrows(SaleNotFinalizedException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));
        verify(invoiceRepository, never()).save(any());
    }

    // 18) A COMPLETED sale whose payment is still PENDING is never finalized for invoicing
    @Test
    void completedSaleWithPendingPaymentIsNotFinalized() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.COMPLETED)
                .paymentStatus("PENDING")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .payments(List.of())
                .build());

        assertThrows(SaleNotFinalizedException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));
        verify(invoiceRepository, never()).save(any());
    }

    // 19) A CANCELLED sale is never finalized for invoicing even if somehow marked paid
    @Test
    void cancelledSaleIsNotFinalized() {
        when(salesClient.getFinalizedSale(42L)).thenReturn(SaleDto.builder()
                .id(42L)
                .saleNumber("SAL-2026-000042")
                .customerId(9L)
                .saleStatus(SaleStatus.CANCELLED)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .build());

        assertThrows(SaleNotFinalizedException.class,
                () -> billingService.createInvoice(new CreateInvoiceRequest(42L)));
        verify(invoiceRepository, never()).save(any());
    }

    private Invoice createInvoiceWithSettings(InvoiceSettingsResponse settings, long sequence) {
        stubHappyPath(settings, sequence);
        billingService.createInvoice(new CreateInvoiceRequest(42L));
        return savedInvoices.get(savedInvoices.size() - 1);
    }

    private void stubHappyPath(InvoiceSettingsResponse settings, long... sequences) {
        when(salesClient.getFinalizedSale(42L)).thenReturn(sale(SaleStatus.COMPLETED));
        when(customerClient.existsById(9L)).thenReturn(true);
        when(invoiceSettingsService.getSettings()).thenReturn(settings);
        doAnswer(invocation -> {
            Invoice created = invocation.getArgument(0);
            savedInvoices.add(created);
            return created;
        }).when(invoiceRepository).save(any());
        when(billingMapper.toInvoiceResponse(any())).thenReturn(new InvoiceResponse());
        if (sequences.length == 1) {
            when(invoiceRepository.nextInvoiceNumberSequence()).thenReturn(sequences[0]);
        } else if (sequences.length > 1) {
            int[] idx = {0};
            when(invoiceRepository.nextInvoiceNumberSequence())
                    .thenAnswer(invocation -> sequences[idx[0]++]);
        }
    }

    private String expectedInvoiceNumber(String prefix, long sequence) {
        return String.format("%s-%d-%06d", prefix, Year.now().getValue(), sequence);
    }

    private InvoiceSettingsResponse defaultSettings() {
        return settings("INV", "INR");
    }

    private InvoiceSettingsResponse settings(String prefix, String currency) {
        return InvoiceSettingsResponse.builder()
                .invoicePrefix(prefix)
                .currency(currency)
                .build();
    }

    private SaleDto sale(SaleStatus status) {
        return SaleDto.builder()
                .id(42L)
                .customerId(9L)
                .saleStatus(status)
                .paymentStatus("PAID")
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("110.00"))
                .build();
    }
}