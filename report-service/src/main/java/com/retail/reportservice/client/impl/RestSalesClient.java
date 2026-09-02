package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.SalesClient;
import com.retail.reportservice.dto.PageResponse;
import com.retail.reportservice.dto.external.ExternalSaleDto;
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
public class RestSalesClient implements SalesClient {

    private final RestClient salesRestClient;

    @Override
    public List<ExternalSaleDto> getAllSales() {
        log.info("Fetching all sales from Sales Service...");
        try {
            PageResponse<ExternalSaleDto> page = salesRestClient.get()
                    .uri("/api/v1/sales")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return page != null ? page.getContent() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch sales from Sales Service", e);
            throw new ExternalServiceException("Sales Service communication failure", e);
        }
    }

    @Override
    public List<ExternalSaleDto> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching sales by date range ({}-{}) from Sales Service...", startDate, endDate);
        List<ExternalSaleDto> allSales = getAllSales();
        if (allSales.isEmpty()) {
            return Collections.emptyList();
        }
        return allSales.stream()
                .filter(s -> s.getSaleDate() != null)
                .filter(s -> {
                    LocalDate date = s.getSaleDate().toLocalDate();
                    boolean afterStart = (startDate == null) || !date.isBefore(startDate);
                    boolean beforeEnd = (endDate == null) || !date.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .toList();
    }
}
