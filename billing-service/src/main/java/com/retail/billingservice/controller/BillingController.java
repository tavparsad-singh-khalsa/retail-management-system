package com.retail.billingservice.controller;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = billingService.createInvoice(request);
        return ResponseEntity.created(URI.create("/api/v1/invoices/" + response.getId())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoice(id));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponse> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getInvoiceByNumber(invoiceNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(billingService.getInvoicesByCustomer(customerId));
    }

    @GetMapping("/sale/{saleId}")
    public ResponseEntity<InvoiceResponse> getInvoiceBySaleId(@PathVariable Long saleId) {
        return ResponseEntity.ok(billingService.getInvoiceBySaleId(saleId));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(billingService.getAllInvoices(pageable));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelInvoice(id));
    }
}
