package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.ProductClient;
import com.retail.reportservice.dto.PageResponse;
import com.retail.reportservice.dto.external.ExternalInventoryDto;
import com.retail.reportservice.dto.external.ExternalProductDto;
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
public class RestProductClient implements ProductClient {

    private final RestClient productRestClient;

    @Override
    public List<ExternalProductDto> getAllProducts() {
        log.info("Fetching all products from Product Service...");
        try {
            PageResponse<ExternalProductDto> page = productRestClient.get()
                    .uri("/api/v1/products")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return page != null ? page.getContent() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch products from Product Service", e);
            throw new ExternalServiceException("Product Service communication failure", e);
        }
    }

    @Override
    public List<ExternalInventoryDto> getAllInventory() {
        log.info("Fetching inventory from Product Service...");
        try {
            List<ExternalInventoryDto> inventory = productRestClient.get()
                    .uri("/api/v1/inventory")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return inventory != null ? inventory : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch inventory from Product Service", e);
            throw new ExternalServiceException("Product Service communication failure", e);
        }
    }
}
