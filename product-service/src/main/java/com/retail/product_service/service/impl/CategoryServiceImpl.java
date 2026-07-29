package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateCategoryRequest;
import com.retail.product_service.dto.request.UpdateCategoryRequest;
import com.retail.product_service.dto.response.CategoryResponse;
import com.retail.product_service.entity.Category;
import com.retail.product_service.exception.CategoryAlreadyExistsException;
import com.retail.product_service.exception.CategoryHierarchyException;
import com.retail.product_service.exception.CategoryNotFoundException;
import com.retail.product_service.repository.CategoryRepository;
import com.retail.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private Category getActiveParentCategory(Long parentId) {
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with ID: " + parentId));

        if (!parentCategory.getIsActive()) {
            throw new CategoryHierarchyException("Cannot assign an inactive category as a parent.");
        }

        return parentCategory;
    }

    @Transactional
    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new CategoryAlreadyExistsException("Category Already Exists");
        }
        Category parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = getActiveParentCategory(request.getParentCategoryId());
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parentCategory)
                .build();

        Category savedCategory = categoryRepository.save(category);


        return mapToResponse(savedCategory);
    }

    @Transactional
    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
                throw new CategoryAlreadyExistsException("Category name already exists: " + request.getName());
            }
            category.setName(request.getName());
        }

        Long currentParentId = category.getParentCategory() != null ? category.getParentCategory().getId() : null;
        Long newParentId = request.getParentCategoryId();

        if (!java.util.Objects.equals(currentParentId, newParentId)) {
            if (newParentId == null) {
                category.setParentCategory(null);
            } else {
                if (newParentId.equals(id)) {
                    throw new CategoryHierarchyException("A category cannot be its own parent.");
                }

                Category parentCategory = getActiveParentCategory(newParentId);
                category.setParentCategory(parentCategory);
            }
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }


    @Transactional(readOnly = true)
    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        if (category.getIsActive()) {
            return;
        }
        category.setIsActive(true);
        categoryRepository.save(category);
    }

    @Transactional
    @Override
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        if (!category.getIsActive()) {
            return;
        }

        if (categoryRepository.existsByParentCategoryAndIsActiveTrue(category)) {
            throw new CategoryHierarchyException(
                    "Cannot deactivate category: It still has active child categories.");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .parentCategoryId(
                        category.getParentCategory() != null
                                ? category.getParentCategory().getId()
                                : null
                )
                .createdAt(category.getCreatedAt())
                .build();
    }
}
