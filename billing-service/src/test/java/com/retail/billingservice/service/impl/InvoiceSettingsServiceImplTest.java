package com.retail.billingservice.service.impl;

import com.retail.billingservice.dto.request.InvoiceSettingsRequest;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.entity.InvoiceSettings;
import com.retail.billingservice.exception.InvoiceSettingsNotFoundException;
import com.retail.billingservice.mapper.BillingMapper;
import com.retail.billingservice.repository.InvoiceSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceSettingsServiceImplTest {

    @Mock private InvoiceSettingsRepository settingsRepository;
    @Mock private BillingMapper billingMapper;
    @InjectMocks private InvoiceSettingsServiceImpl invoiceSettingsService;

    private final InvoiceSettings existing = InvoiceSettings.builder().id(1L).build();

    @Test
    void getSettingsReturnsMappedValueWhenRowExists() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        InvoiceSettingsResponse response = invoiceSettingsService.getSettings();

        verify(billingMapper).toSettingsResponse(existing);
        assertNotNull(response);
    }

    @Test
    void getSettingsThrowsWhenRowMissing() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvoiceSettingsNotFoundException.class,
                () -> invoiceSettingsService.getSettings());
    }

    @Test
    void updatePersistsAllProvidedFields() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        InvoiceSettingsRequest request = configuredRequest();
        request.setAllowPartialPayment(true);
        invoiceSettingsService.updateSettings(request);

        InvoiceSettings saved = captureSaved();
        assertEquals("Retail India", saved.getShopName());
        assertEquals("MG Road, Bengaluru", saved.getAddress());
        assertEquals("+91 98765 43210", saved.getPhoneNumber());
        assertEquals("hello@retail.example", saved.getEmail());
        assertEquals("https://instagram.com/retailshop", saved.getInstagramUrl());
        assertEquals("https://maps.google.com/?q=retail", saved.getGoogleMapsUrl());
        assertEquals("https://g.page/r/retail", saved.getGoogleReviewUrl());
        assertEquals("INV", saved.getInvoicePrefix());
        assertEquals("Thank you for shopping with us", saved.getInvoiceTagline());
        assertEquals("Powered by Retail Suite", saved.getFooterText());
        assertEquals(30, saved.getPaymentTerms());
        assertEquals("INR", saved.getCurrency());
        assertTrue(saved.getAllowPartialPayment());
    }

    @Test
    void updateNormalizesBlankOptionalsToNull() {
        InvoiceSettingsRequest request = configuredRequest();
        request.setEmail("");
        request.setInstagramUrl("   ");
        request.setGoogleMapsUrl("");
        request.setGoogleReviewUrl(" ");
        request.setGstin("\t");
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        invoiceSettingsService.updateSettings(request);

        InvoiceSettings saved = captureSaved();
        assertNull(saved.getEmail());
        assertNull(saved.getInstagramUrl());
        assertNull(saved.getGoogleMapsUrl());
        assertNull(saved.getGoogleReviewUrl());
        assertNull(saved.getGstin());
    }

    @Test
    void updateCanDisableTaxCompletely() {
        InvoiceSettingsRequest request = configuredRequest();
        request.setTaxEnabled(false);
        request.setTaxName("GST");
        request.setTaxRate(BigDecimal.ZERO);
        request.setShowTax(false);
        request.setGstin(null);
        request.setShowGstin(false);
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        invoiceSettingsService.updateSettings(request);

        InvoiceSettings saved = captureSaved();
        assertFalse(saved.getTaxEnabled());
        assertEquals("GST", saved.getTaxName());
        assertEquals(0, saved.getTaxRate().compareTo(BigDecimal.ZERO));
        assertFalse(saved.getShowTax());
        assertNull(saved.getGstin());
        assertFalse(saved.getShowGstin());
    }

    @Test
    void updateCanEnableTaxWithConfiguredRate() {
        InvoiceSettingsRequest request = configuredRequest();
        request.setTaxEnabled(true);
        request.setTaxName("GST");
        request.setTaxRate(new BigDecimal("18.00"));
        request.setShowTax(true);
        request.setGstin("29ABCDE1234F1Z5");
        request.setShowGstin(true);
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        invoiceSettingsService.updateSettings(request);

        InvoiceSettings saved = captureSaved();
        assertTrue(saved.getTaxEnabled());
        assertEquals("GST", saved.getTaxName());
        assertEquals(0, saved.getTaxRate().compareTo(new BigDecimal("18.00")));
        assertTrue(saved.getShowTax());
        assertEquals("29ABCDE1234F1Z5", saved.getGstin());
        assertTrue(saved.getShowGstin());
    }

    @Test
    void displayTogglesPersistIndependently() {
        InvoiceSettingsRequest request = configuredRequest();
        request.setShowShopName(true);
        request.setShowAddress(false);
        request.setShowPhone(true);
        request.setShowEmail(false);
        request.setShowInstagram(true);
        request.setShowGoogleMaps(false);
        request.setShowGoogleReview(true);
        request.setShowTagline(false);
        request.setShowPaymentTerms(true);
        request.setShowFooter(false);
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(billingMapper.toSettingsResponse(any())).thenReturn(new InvoiceSettingsResponse());

        invoiceSettingsService.updateSettings(request);

        InvoiceSettings saved = captureSaved();
        assertTrue(saved.getShowShopName());
        assertFalse(saved.getShowAddress());
        assertTrue(saved.getShowPhone());
        assertFalse(saved.getShowEmail());
        assertTrue(saved.getShowInstagram());
        assertFalse(saved.getShowGoogleMaps());
        assertTrue(saved.getShowGoogleReview());
        assertFalse(saved.getShowTagline());
        assertTrue(saved.getShowPaymentTerms());
        assertFalse(saved.getShowFooter());
    }

    @Test
    void updateSettingsThrowsWhenRowMissing() {
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvoiceSettingsNotFoundException.class,
                () -> invoiceSettingsService.updateSettings(configuredRequest()));
    }

    private InvoiceSettings captureSaved() {
        ArgumentCaptor<InvoiceSettings> captor = ArgumentCaptor.forClass(InvoiceSettings.class);
        verify(settingsRepository).save(captor.capture());
        return captor.getValue();
    }

    private InvoiceSettingsRequest configuredRequest() {
        return InvoiceSettingsRequest.builder()
                .shopName("Retail India")
                .address("MG Road, Bengaluru")
                .phoneNumber("+91 98765 43210")
                .email("hello@retail.example")
                .instagramUrl("https://instagram.com/retailshop")
                .googleMapsUrl("https://maps.google.com/?q=retail")
                .googleReviewUrl("https://g.page/r/retail")
                .invoicePrefix("INV")
                .invoiceTagline("Thank you for shopping with us")
                .footerText("Powered by Retail Suite")
                .paymentTerms(30)
                .currency("INR")
                .showShopName(true)
                .showAddress(true)
                .showPhone(true)
                .showEmail(true)
                .showInstagram(true)
                .showGoogleMaps(true)
                .showGoogleReview(true)
                .showTagline(true)
                .showPaymentTerms(true)
                .showFooter(true)
                .taxEnabled(false)
                .taxName("GST")
                .taxRate(new BigDecimal("0.00"))
                .showTax(false)
                .gstin(null)
                .showGstin(false)
                .allowPartialPayment(false)
                .build();
    }
}