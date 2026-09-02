package com.retail.billingservice.service.impl;

import com.retail.billingservice.dto.request.InvoiceSettingsRequest;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.entity.InvoiceSettings;
import com.retail.billingservice.exception.InvoiceSettingsNotFoundException;
import com.retail.billingservice.mapper.BillingMapper;
import com.retail.billingservice.repository.InvoiceSettingsRepository;
import com.retail.billingservice.service.InvoiceSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceSettingsServiceImpl implements InvoiceSettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final InvoiceSettingsRepository settingsRepository;
    private final BillingMapper billingMapper;

    @Override
    @Transactional(readOnly = true)
    public InvoiceSettingsResponse getSettings() {
        return billingMapper.toSettingsResponse(
                settingsRepository.findById(SETTINGS_ID)
                        .orElseThrow(() -> new InvoiceSettingsNotFoundException(
                                "Invoice settings not found")));
    }

    @Override
    @Transactional
    public InvoiceSettingsResponse updateSettings(InvoiceSettingsRequest request) {
        InvoiceSettings settings = settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new InvoiceSettingsNotFoundException(
                        "Invoice settings not found"));

        settings.setShopName(request.getShopName());
        settings.setAddress(request.getAddress());
        settings.setPhoneNumber(request.getPhoneNumber());
        settings.setEmail(trimToNull(request.getEmail()));
        settings.setInstagramUrl(trimToNull(request.getInstagramUrl()));
        settings.setGoogleMapsUrl(trimToNull(request.getGoogleMapsUrl()));
        settings.setGoogleReviewUrl(trimToNull(request.getGoogleReviewUrl()));

        settings.setInvoicePrefix(request.getInvoicePrefix());
        settings.setInvoiceTagline(request.getInvoiceTagline());
        settings.setFooterText(request.getFooterText());
        settings.setPaymentTerms(request.getPaymentTerms());
        settings.setCurrency(request.getCurrency());

        settings.setShowShopName(request.getShowShopName());
        settings.setShowAddress(request.getShowAddress());
        settings.setShowPhone(request.getShowPhone());
        settings.setShowEmail(request.getShowEmail());
        settings.setShowInstagram(request.getShowInstagram());
        settings.setShowGoogleMaps(request.getShowGoogleMaps());
        settings.setShowGoogleReview(request.getShowGoogleReview());
        settings.setShowTagline(request.getShowTagline());
        settings.setShowPaymentTerms(request.getShowPaymentTerms());
        settings.setShowFooter(request.getShowFooter());

        settings.setTaxEnabled(request.getTaxEnabled());
        settings.setTaxName(request.getTaxName());
        settings.setTaxRate(request.getTaxRate());
        settings.setShowTax(request.getShowTax());
        settings.setGstin(trimToNull(request.getGstin()));
        settings.setShowGstin(request.getShowGstin());

        settings.setAllowPartialPayment(request.getAllowPartialPayment());

        return billingMapper.toSettingsResponse(settingsRepository.save(settings));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}