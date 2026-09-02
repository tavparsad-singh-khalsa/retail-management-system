package com.retail.purchase_service.controller;

import com.retail.purchase_service.dto.request.CreatePurchaseRequest;
import com.retail.purchase_service.dto.response.PurchaseResponse;
import com.retail.purchase_service.enums.PurchaseStatus;
import com.retail.purchase_service.service.interfaces.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    // --- Create Operations ---

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request) {
        PurchaseResponse response = purchaseService.createPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Read Operations ---

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{purchaseNumber}")
    public ResponseEntity<PurchaseResponse> getPurchaseByNumber(
            @PathVariable String purchaseNumber) {
        PurchaseResponse response = purchaseService.getPurchaseByNumber(purchaseNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> getAllPurchases(@PageableDefault(size = 20) Pageable pageable) {
        Page<PurchaseResponse> responses = purchaseService.getAllPurchases(pageable);
        return ResponseEntity.ok(responses);
    }

    // --- Search Operations ---

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByStatus(
            @PathVariable PurchaseStatus status) {
        List<PurchaseResponse> responses = purchaseService.getPurchasesByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesBySupplier(
            @PathVariable Long supplierId) {
        List<PurchaseResponse> responses = purchaseService.getPurchasesBySupplier(supplierId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/supplier/{supplierId}/status/{status}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesBySupplierAndStatus(
            @PathVariable Long supplierId,
            @PathVariable PurchaseStatus status) {
        List<PurchaseResponse> responses = purchaseService.getPurchasesBySupplierAndStatus(supplierId, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PurchaseResponse> responses = purchaseService.getPurchasesByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    // --- Business Actions ---

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PurchaseResponse> approvePurchase(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.approvePurchase(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponse> receivePurchase(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.receivePurchase(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseResponse> cancelPurchase(@PathVariable Long id) {
        PurchaseResponse response = purchaseService.cancelPurchase(id);
        return ResponseEntity.ok(response);
    }
}