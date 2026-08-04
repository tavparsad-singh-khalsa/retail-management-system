package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.PurchaseClient;
import com.retail.reportservice.dto.external.ExternalPurchaseDto;
import com.retail.reportservice.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestPurchaseClient implements PurchaseClient {

    private final RestClient purchaseRestClient;

    @Override
    public List<ExternalPurchaseDto> getAllPurchases() {
        log.info("Fetching all purchases from Purchase Service...");
        try {
            List<ExternalPurchaseDto> purchases = purchaseRestClient.get()
                    .uri("/api/v1/purchases")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return purchases != null ? purchases : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch purchases from Purchase Service", e);
            throw new ExternalServiceException("Purchase Service communication failure", e);
        }
    }

    @Override
    public List<ExternalPurchaseDto> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching purchases by date range ({}-{}) from Purchase Service...", startDate, endDate);
        List<ExternalPurchaseDto> allPurchases = getAllPurchases();
        if (allPurchases.isEmpty()) {
            return Collections.emptyList();
        }
        return allPurchases.stream()
                .filter(p -> p.getCreatedAt() != null)
                .filter(p -> {
                    LocalDate date = p.getCreatedAt().toLocalDate();
                    boolean afterStart = (startDate == null) || !date.isBefore(startDate);
                    boolean beforeEnd = (endDate == null) || !date.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .toList();
    }
}
