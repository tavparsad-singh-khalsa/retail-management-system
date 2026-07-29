package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateAttributeRequest;
import com.retail.product_service.dto.request.UpdateAttributeRequest;
import com.retail.product_service.dto.response.AttributeResponse;
import com.retail.product_service.service.AttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @PostMapping
    public ResponseEntity<AttributeResponse> createAttribute(@Valid @RequestBody CreateAttributeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attributeService.createAttribute(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeResponse> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request) {
        return ResponseEntity.ok(attributeService.updateAttribute(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeResponse> getAttributeById(@PathVariable Long id) {
        return ResponseEntity.ok(attributeService.getAttributeById(id));
    }

    @GetMapping
    public ResponseEntity<List<AttributeResponse>> getAllAttributes() {
        return ResponseEntity.ok(attributeService.getAllAttributes());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateAttribute(@PathVariable Long id) {
        attributeService.activateAttribute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAttribute(@PathVariable Long id) {
        attributeService.deactivateAttribute(id);
        return ResponseEntity.noContent().build();
    }
}