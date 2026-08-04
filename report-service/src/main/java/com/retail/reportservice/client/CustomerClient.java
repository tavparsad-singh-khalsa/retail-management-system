package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalCustomerDto;

import java.util.List;

public interface CustomerClient {

    List<ExternalCustomerDto> getAllCustomers();
}
