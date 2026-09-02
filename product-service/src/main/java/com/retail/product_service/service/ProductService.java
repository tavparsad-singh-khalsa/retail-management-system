package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateProductRequest;
import com.retail.product_service.dto.request.UpdateProductRequest;
import com.retail.product_service.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    List<ProductResponse> getAllActiveProducts();

    void activateProduct(Long id);

    void deactivateProduct(Long id);

    // ⭐ Simplified to a single, clean method signature
    ProductResponse getProductByCode(String productCode);
}