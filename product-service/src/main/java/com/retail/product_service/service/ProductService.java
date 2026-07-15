package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateProductRequest;
import com.retail.product_service.dto.request.UpdateProductRequest;
import com.retail.product_service.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse getProductByProductCode(String productCode);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getAllActiveProducts();

    void activateProduct(Long id);

    void deactivateProduct(Long id);
}