package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateAttributeValueRequest;
import com.retail.product_service.dto.request.UpdateAttributeValueRequest;
import com.retail.product_service.dto.response.AttributeValueResponse;
import com.retail.product_service.service.AttributeValueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attribute-values")
@RequiredArgsConstructor
public class AttributeValueController {

    private final AttributeValueService attributeValueService;

    @PostMapping
    public ResponseEntity<AttributeValueResponse> createAttributeValue(@Valid @RequestBody CreateAttributeValueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attributeValueService.createAttributeValue(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeValueResponse> updateAttributeValue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeValueRequest request) {
        return ResponseEntity.ok(attributeValueService.updateAttributeValue(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeValueResponse> getAttributeValueById(@PathVariable Long id) {
        return ResponseEntity.ok(attributeValueService.getAttributeValueById(id));
    }

    @GetMapping
    public ResponseEntity<List<AttributeValueResponse>> getAllAttributeValues() {
        return ResponseEntity.ok(attributeValueService.getAllAttributeValues());
    }

    @GetMapping("/attribute/{attributeId}")
    public ResponseEntity<List<AttributeValueResponse>> getAttributeValuesByAttributeId(@PathVariable Long attributeId) {
        return ResponseEntity.ok(attributeValueService.getAttributeValuesByAttributeId(attributeId));
    }

    // ⭐ New Endpoint: Fetches only ACTIVE values for a specific Attribute
    @GetMapping("/attribute/{attributeId}/active")
    public ResponseEntity<List<AttributeValueResponse>> getActiveAttributeValuesByAttributeId(@PathVariable Long attributeId) {
        return ResponseEntity.ok(attributeValueService.getActiveAttributeValuesByAttributeId(attributeId));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateAttributeValue(@PathVariable Long id) {
        attributeValueService.activateAttributeValue(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAttributeValue(@PathVariable Long id) {
        attributeValueService.deactivateAttributeValue(id);
        return ResponseEntity.noContent().build();
    }
}