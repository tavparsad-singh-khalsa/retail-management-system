package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateProductImageRequest;
import com.retail.product_service.dto.request.UpdateProductImageRequest;
import com.retail.product_service.dto.response.ProductImageResponse;
import com.retail.product_service.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping
    public ResponseEntity<ProductImageResponse> createProductImage(@Valid @RequestBody CreateProductImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productImageService.createProductImage(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductImageResponse> updateProductImage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductImageRequest request) {
        return ResponseEntity.ok(productImageService.updateProductImage(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageResponse> getProductImageById(@PathVariable Long id) {
        return ResponseEntity.ok(productImageService.getProductImageById(id));
    }

    // ⭐ Get all images for a base product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getProductImagesByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getProductImagesByProductId(productId));
    }

    // ⭐ Get all images for a specific variant (e.g., the blue t-shirt images)
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<ProductImageResponse>> getProductImagesByVariantId(@PathVariable Long variantId) {
        return ResponseEntity.ok(productImageService.getProductImagesByVariantId(variantId));
    }

    // ⭐ Business endpoint: Set an image as the primary thumbnail
    @PatchMapping("/{id}/primary")
    public ResponseEntity<Void> setPrimaryImage(@PathVariable Long id) {
        productImageService.setPrimaryImage(id);
        return ResponseEntity.noContent().build();
    }

    // ⭐ Soft delete / deactivate
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProductImage(@PathVariable Long id) {
        productImageService.deactivateProductImage(id);
        return ResponseEntity.noContent().build();
    }
}