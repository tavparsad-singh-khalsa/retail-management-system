package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateVariantAttributeRequest;
import com.retail.product_service.dto.request.UpdateVariantAttributeRequest;
import com.retail.product_service.dto.response.VariantAttributeResponse;
import com.retail.product_service.service.VariantAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variant-attributes")
@RequiredArgsConstructor
public class VariantAttributeController {

    private final VariantAttributeService variantAttributeService;

    @PostMapping
    public ResponseEntity<VariantAttributeResponse> createVariantAttribute(@Valid @RequestBody CreateVariantAttributeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(variantAttributeService.createVariantAttribute(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VariantAttributeResponse> updateVariantAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantAttributeRequest request) {
        return ResponseEntity.ok(variantAttributeService.updateVariantAttribute(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantAttributeResponse> getVariantAttributeById(@PathVariable Long id) {
        return ResponseEntity.ok(variantAttributeService.getVariantAttributeById(id));
    }

    // ⭐ NEW: Retrieve all VariantAttribute records
    @GetMapping
    public ResponseEntity<List<VariantAttributeResponse>> getAllVariantAttributes() {
        return ResponseEntity.ok(variantAttributeService.getAllVariantAttributes());
    }

    // ⭐ Relationship endpoint: Fetches all attributes assigned to a specific variant
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<VariantAttributeResponse>> getVariantAttributesByVariantId(@PathVariable Long variantId) {
        return ResponseEntity.ok(variantAttributeService.getVariantAttributesByVariantId(variantId));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateVariantAttribute(@PathVariable Long id) {
        variantAttributeService.activateVariantAttribute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateVariantAttribute(@PathVariable Long id) {
        variantAttributeService.deactivateVariantAttribute(id);
        return ResponseEntity.noContent().build();
    }
}