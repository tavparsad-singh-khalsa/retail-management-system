package com.retail.billingservice.service;

import com.retail.billingservice.dto.request.InvoiceSettingsRequest;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;

public interface InvoiceSettingsService {

    InvoiceSettingsResponse getSettings();

    InvoiceSettingsResponse updateSettings(InvoiceSettingsRequest request);

}