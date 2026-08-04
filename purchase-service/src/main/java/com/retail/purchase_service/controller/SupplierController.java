package com.retail.purchase_service.controller;

import com.retail.purchase_service.dto.request.CreateSupplierRequest;
import com.retail.purchase_service.dto.request.UpdateSupplierRequest;
import com.retail.purchase_service.dto.response.SupplierResponse;
import com.retail.purchase_service.service.interfaces.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getAllActiveSuppliers());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateSupplier(@PathVariable Long id) {
        supplierService.activateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
