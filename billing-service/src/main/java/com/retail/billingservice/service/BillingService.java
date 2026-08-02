package com.retail.billingservice.service;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.request.MarkPaidRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;

import java.util.List;

public interface BillingService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoice(Long invoiceId);

    InvoiceResponse getInvoiceByNumber(String invoiceNumber);

    List<InvoiceResponse> getAllInvoices();

    List<InvoiceResponse> getInvoicesByCustomer(Long customerId);

    InvoiceResponse markPaid(Long invoiceId, MarkPaidRequest request);

    InvoiceResponse cancelInvoice(Long invoiceId);

}
