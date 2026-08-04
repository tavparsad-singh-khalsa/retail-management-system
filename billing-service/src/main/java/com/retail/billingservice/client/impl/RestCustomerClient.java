package com.retail.billingservice.client.impl;

import com.retail.billingservice.client.CustomerClient;
import com.retail.billingservice.exception.CustomerNotFoundException;
import com.retail.billingservice.exception.CustomerServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestCustomerClient implements CustomerClient {

    private final RestClient customerRestClient;

    private static final String GET_CUSTOMER = "/api/customers/%d";

    @Override
    public boolean existsById(Long customerId) {
        log.info("Checking if customer exists in Customer Service: id={}...", customerId);
        try {
            customerRestClient.get()
                    .uri(String.format(GET_CUSTOMER, customerId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Customer Service returned 4xx error checking customer {}. Status: {}", customerId, res.getStatusCode());
                        throw new CustomerNotFoundException("Customer not found: " + customerId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Customer Service returned error checking customer {}. Status: {}", customerId, res.getStatusCode());
                        throw new CustomerServiceException("Customer Service error: " + res.getStatusCode());
                    })
                    .toBodilessEntity();
            
            log.info("Customer {} confirmed to exist in Customer Service", customerId);
            return true;
        } catch (CustomerNotFoundException e) {
            log.warn("Customer {} not found in Customer Service", customerId);
            return false;
        } catch (RestClientException e) {
            log.error("REST client communication error checking customer {} in Customer Service", customerId, e);
            throw new CustomerServiceException("Failed to verify customer existence", e);
        } catch (Exception e) {
            log.error("Failed to check customer existence for id {} in Customer Service", customerId, e);
            throw new CustomerServiceException("Failed to verify customer existence", e);
        }
    }
}
