package com.retail.billingservice.client.impl;

import com.retail.billingservice.client.SalesClient;
import com.retail.billingservice.exception.SaleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestSalesClient implements SalesClient {

    private final RestClient salesRestClient;

    private static final String GET_SALE = "/api/v1/sales/%d";

    @Override
    public SaleDto getFinalizedSale(Long saleId) {
        log.info("Fetching finalized sale from Sales Service: id={}...", saleId);
        try {
            SaleDto response = salesRestClient.get()
                    .uri(String.format(GET_SALE, saleId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Sales Service returned 4xx error fetching sale {}. Status: {}", saleId, res.getStatusCode());
                        throw new SaleNotFoundException("Sale not found: " + saleId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Sales Service returned error fetching sale {}. Status: {}", saleId, res.getStatusCode());
                        throw new RuntimeException("Sales Service error: " + res.getStatusCode());
                    })
                    .body(SaleDto.class);
            
            if (response == null) {
                log.warn("Empty response from Sales Service for sale id: {}", saleId);
                throw new SaleNotFoundException("Sale not found: " + saleId);
            }
            
            log.info("Successfully fetched sale {} from Sales Service", saleId);
            return response;
        } catch (Exception e) {
            if (e instanceof SaleNotFoundException) {
                throw e;
            }
            log.error("Failed to fetch sale {} from Sales Service", saleId, e);
            throw new RuntimeException("Failed to fetch sale from Sales Service", e);
        }
    }
}
