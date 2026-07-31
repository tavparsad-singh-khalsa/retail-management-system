package com.retail.billingservice.service;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;

import java.util.List;

public interface BillingService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoice(Long id);

    List<InvoiceResponse> getAllInvoices();
}
