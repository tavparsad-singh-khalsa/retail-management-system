package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateProductVariantRequest;
import com.retail.product_service.dto.request.UpdateProductVariantRequest;
import com.retail.product_service.dto.response.ProductVariantResponse;
import com.retail.product_service.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ResponseEntity<ProductVariantResponse> createProductVariant(@Valid @RequestBody CreateProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productVariantService.createProductVariant(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantResponse> updateProductVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductVariantRequest request) {
        return ResponseEntity.ok(productVariantService.updateProductVariant(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getVariantById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductVariantResponse>> getAllVariants() {
        return ResponseEntity.ok(productVariantService.getAllVariants());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductVariantResponse> getVariantBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productVariantService.getVariantBySku(sku));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductVariantResponse> getVariantByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productVariantService.getVariantByBarcode(barcode));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateVariant(@PathVariable Long id) {
        productVariantService.activateVariant(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateVariant(@PathVariable Long id) {
        productVariantService.deactivateVariant(id);
        return ResponseEntity.noContent().build();
    }
}