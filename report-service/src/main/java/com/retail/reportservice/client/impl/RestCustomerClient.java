package com.retail.reportservice.client.impl;

import com.retail.reportservice.client.CustomerClient;
import com.retail.reportservice.dto.PageResponse;
import com.retail.reportservice.dto.external.ExternalCustomerDto;
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
public class RestCustomerClient implements CustomerClient {

    private final RestClient customerRestClient;

    @Override
    public List<ExternalCustomerDto> getAllCustomers() {
        log.info("Fetching all customers from Customer Service...");
        try {
            PageResponse<ExternalCustomerDto> page = customerRestClient.get()
                    .uri("/api/customers")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return page != null ? page.getContent() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch customers from Customer Service", e);
            throw new ExternalServiceException("Customer Service communication failure", e);
        }
    }
}
