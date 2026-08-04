package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.ProductClient;
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
            List<ExternalProductDto> products = productRestClient.get()
                    .uri("/api/v1/products")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return products != null ? products : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch products from Product Service", e);
            throw new ExternalServiceException("Product Service communication failure", e);
        }
    }
}
