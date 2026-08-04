package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.BillingClient;
import com.retail.reportservice.dto.external.ExternalInvoiceDto;
import com.retail.reportservice.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestBillingClient implements BillingClient {

    private final RestClient billingRestClient;

    @Override
    public List<ExternalInvoiceDto> getAllInvoices() {
        log.info("Fetching all invoices from Billing Service...");
        try {
            List<ExternalInvoiceDto> invoices = billingRestClient.get()
                    .uri("/api/v1/invoices")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return invoices != null ? invoices : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch invoices from Billing Service", e);
            throw new ExternalServiceException("Billing Service communication failure", e);
        }
    }
}
