package com.retail.billingservice.controller;

import com.retail.billingservice.dto.request.InvoiceSettingsRequest;
import com.retail.billingservice.dto.response.InvoiceSettingsResponse;
import com.retail.billingservice.service.InvoiceSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoice-settings")
@RequiredArgsConstructor
public class InvoiceSettingsController {

    private final InvoiceSettingsService invoiceSettingsService;

    @GetMapping
    public ResponseEntity<InvoiceSettingsResponse> getSettings() {
        return ResponseEntity.ok(invoiceSettingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<InvoiceSettingsResponse> updateSettings(
            @Valid @RequestBody InvoiceSettingsRequest request) {
        return ResponseEntity.ok(invoiceSettingsService.updateSettings(request));
    }

}