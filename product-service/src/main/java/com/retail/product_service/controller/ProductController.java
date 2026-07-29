package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateProductRequest;
import com.retail.product_service.dto.request.UpdateProductRequest;
import com.retail.product_service.dto.response.ProductResponse;
import com.retail.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductResponse>> getAllActiveProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<ProductResponse> getProductByCode(@PathVariable String productCode) {
        return ResponseEntity.ok(productService.getProductByCode(productCode));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }
}