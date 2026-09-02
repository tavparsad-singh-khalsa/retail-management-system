package com.retail.sales_service.client.impl;

import com.retail.sales_service.client.BillingClient;
import com.retail.sales_service.dto.integration.response.InvoiceSettingsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Slf4j
@Service
public class RestBillingClient implements BillingClient {

    private final RestClient billingRestClient;

    public RestBillingClient(@Qualifier("billingRestClient") RestClient billingRestClient) {
        this.billingRestClient = billingRestClient;
    }

    private static final String INVOICE_BY_SALE_ID = "/api/v1/invoices/sale/%d";
    private static final String INVOICE_SETTINGS = "/api/v1/invoice-settings";

    @Override
    public boolean hasActiveInvoice(Long saleId) {
        log.info("Checking for active invoice for sale ID: {}", saleId);
        try {
            Object response = billingRestClient.get()
                    .uri(String.format(INVOICE_BY_SALE_ID, saleId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        log.debug("No active invoice found for sale ID: {}. Status: {}", saleId, res.getStatusCode());
                    })
                    .body(Object.class);
            return response != null;
        } catch (Exception e) {
            log.debug("No active invoice found for sale ID: {}", saleId);
            return false;
        }
    }

    @Override
    public InvoiceSettingsResponse getInvoiceSettings() {
        try {
            InvoiceSettingsResponse settings = billingRestClient.get()
                    .uri(INVOICE_SETTINGS)
                    .retrieve()
                    .body(InvoiceSettingsResponse.class);
            if (settings == null) {
                log.warn("Invoice settings endpoint returned an empty response, defaulting to tax disabled");
                return taxDisabledSettings();
            }
            return settings;
        } catch (Exception e) {
            log.warn("Could not retrieve invoice settings, defaulting to tax disabled: {}", e.getMessage());
            return taxDisabledSettings();
        }
    }

    private InvoiceSettingsResponse taxDisabledSettings() {
        return InvoiceSettingsResponse.builder()
                .taxEnabled(false)
                .taxRate(BigDecimal.ZERO)
                .build();
    }
}
