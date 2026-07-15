package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateCategoryRequest;
import com.retail.product_service.dto.request.UpdateCategoryRequest;
import com.retail.product_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(
            CreateCategoryRequest request
    );

    CategoryResponse updateCategory(
            Long id,
            UpdateCategoryRequest request
    );

    CategoryResponse getCategoryById(
            Long id
    );

    List<CategoryResponse> getAllCategories();

    void deactivateCategory(
            Long id
    );

    void activateCategory(
            Long id
    );


}
