package com.retail.billingservice.service;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillingService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoice(Long invoiceId);

    InvoiceResponse getInvoiceByNumber(String invoiceNumber);

    Page<InvoiceResponse> getAllInvoices(Pageable pageable);

    List<InvoiceResponse> getInvoicesByCustomer(Long customerId);

    InvoiceResponse getInvoiceBySaleId(Long saleId);

    InvoiceResponse cancelInvoice(Long invoiceId);

}
