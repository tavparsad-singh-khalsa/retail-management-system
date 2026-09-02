package com.retail.sales_service.client;

import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;

public interface BillingClient {

    boolean hasActiveInvoice(Long saleId);

    InvoiceSettingsResponse getInvoiceSettings();
}
