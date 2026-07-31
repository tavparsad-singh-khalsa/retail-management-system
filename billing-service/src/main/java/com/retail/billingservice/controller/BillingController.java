package com.retail.billingservice.controller;

import com.retail.billingservice.dto.request.CreateInvoiceRequest;
import com.retail.billingservice.dto.response.InvoiceResponse;
import com.retail.billingservice.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(billingService.getInvoice(id));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAll() {
        return ResponseEntity.ok(billingService.getAllInvoices());
    }
}
