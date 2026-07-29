package com.retail.product_service.controller;

import com.retail.product_service.dto.request.CreateCategoryRequest;
import com.retail.product_service.dto.request.UpdateCategoryRequest;
import com.retail.product_service.dto.response.CategoryResponse;
import com.retail.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 1️⃣ Create a new category
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    // 2️⃣ Get all categories
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // 3️⃣ Get a single category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // 4️⃣ Update an existing category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // 5️⃣ Delete (or deactivate) a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable Long id) {

        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }

    // 6️⃣ Activate a category
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCategory(
            @PathVariable Long id) {

        categoryService.activateCategory(id);
        return ResponseEntity.noContent().build(); // ⭐ Fixed: Now returns 204 No Content
    }
}