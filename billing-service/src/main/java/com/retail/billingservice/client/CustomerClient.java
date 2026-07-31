package com.retail.billingservice.client;

import org.springframework.stereotype.Component;

/**
 * Simple RestClient stub for Customer service.
 */
@Component
public class CustomerClient {

    public boolean existsById(Long customerId) {
        // Stub - replace with REST call to Customer Service
        return true; // assume exists for scaffolding
    }
}
