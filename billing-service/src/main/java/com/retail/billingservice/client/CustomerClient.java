package com.retail.billingservice.client;

import com.retail.billingservice.dto.customer.CustomerDto;

public interface CustomerClient {

    boolean existsById(Long customerId);

    CustomerDto getCustomer(Long customerId);

}