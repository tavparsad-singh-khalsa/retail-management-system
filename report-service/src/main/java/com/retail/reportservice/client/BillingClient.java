package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalInvoiceDto;

import java.util.List;

public interface BillingClient {

    List<ExternalInvoiceDto> getAllInvoices();
}
