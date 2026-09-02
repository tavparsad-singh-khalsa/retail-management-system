package com.retail.billingservice.repository;

import com.retail.billingservice.entity.InvoiceSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class InvoiceSettingsPersistenceTest {

    @Autowired private InvoiceSettingsRepository repository;

    @Test
    void defaultSettingsRoundTrip() {
        repository.save(InvoiceSettings.builder().id(1L).build());

        InvoiceSettings loaded = repository.findById(1L).orElseThrow();

        assertEquals("INV", loaded.getInvoicePrefix());
        assertEquals("INR", loaded.getCurrency());
        assertEquals(30, loaded.getPaymentTerms());
        assertFalse(loaded.getTaxEnabled());
        assertEquals("GST", loaded.getTaxName());
        assertEquals(0, loaded.getTaxRate().compareTo(BigDecimal.ZERO));
        assertFalse(loaded.getShowTax());
        assertFalse(loaded.getShowGstin());
        assertFalse(loaded.getAllowPartialPayment());
        assertTrue(loaded.getShowShopName());
        assertNotNull(loaded.getUpdatedAt());
    }

    @Test
    void fullyConfiguredSettingsPersistAllFields() {
        InvoiceSettings settings = InvoiceSettings.builder()
                .id(1L)
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
                .showAddress(false)
                .showPhone(true)
                .showEmail(false)
                .showInstagram(true)
                .showGoogleMaps(false)
                .showGoogleReview(true)
                .showTagline(false)
                .showPaymentTerms(true)
                .showFooter(false)
                .taxEnabled(true)
                .taxName("GST")
                .taxRate(new BigDecimal("18.00"))
                .showTax(true)
                .gstin("29ABCDE1234F1Z5")
                .showGstin(true)
                .allowPartialPayment(true)
                .build();

        repository.save(settings);

        InvoiceSettings loaded = repository.findById(1L).orElseThrow();
        assertEquals("Retail India", loaded.getShopName());
        assertEquals("MG Road, Bengaluru", loaded.getAddress());
        assertEquals("+91 98765 43210", loaded.getPhoneNumber());
        assertEquals("hello@retail.example", loaded.getEmail());
        assertEquals("https://instagram.com/retailshop", loaded.getInstagramUrl());
        assertEquals("https://maps.google.com/?q=retail", loaded.getGoogleMapsUrl());
        assertEquals("https://g.page/r/retail", loaded.getGoogleReviewUrl());
        assertEquals("INV", loaded.getInvoicePrefix());
        assertEquals("Thank you for shopping with us", loaded.getInvoiceTagline());
        assertEquals("Powered by Retail Suite", loaded.getFooterText());
        assertEquals(30, loaded.getPaymentTerms());
        assertEquals("INR", loaded.getCurrency());
        assertTrue(loaded.getShowShopName());
        assertFalse(loaded.getShowAddress());
        assertTrue(loaded.getShowPhone());
        assertFalse(loaded.getShowEmail());
        assertTrue(loaded.getShowInstagram());
        assertFalse(loaded.getShowGoogleMaps());
        assertTrue(loaded.getShowGoogleReview());
        assertFalse(loaded.getShowTagline());
        assertTrue(loaded.getShowPaymentTerms());
        assertFalse(loaded.getShowFooter());
        assertTrue(loaded.getTaxEnabled());
        assertEquals("GST", loaded.getTaxName());
        assertEquals(0, loaded.getTaxRate().compareTo(new BigDecimal("18.00")));
        assertTrue(loaded.getShowTax());
        assertEquals("29ABCDE1234F1Z5", loaded.getGstin());
        assertTrue(loaded.getShowGstin());
        assertTrue(loaded.getAllowPartialPayment());
        assertNotNull(loaded.getUpdatedAt());
    }
}