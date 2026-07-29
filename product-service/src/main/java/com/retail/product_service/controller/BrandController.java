package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateBrandRequest;
import com.retail.product_service.dto.request.UpdateBrandRequest;
import com.retail.product_service.dto.response.BrandResponse;
import com.retail.product_service.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateBrand(@PathVariable Long id) {
        brandService.activateBrand(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateBrand(@PathVariable Long id) {
        brandService.deactivateBrand(id);
        return ResponseEntity.noContent().build();
    }
}