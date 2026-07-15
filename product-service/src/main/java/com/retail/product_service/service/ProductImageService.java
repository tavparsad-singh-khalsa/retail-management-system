package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateProductImageRequest;
import com.retail.product_service.dto.request.UpdateProductImageRequest;
import com.retail.product_service.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse createProductImage(CreateProductImageRequest request);

    ProductImageResponse updateProductImage(Long id, UpdateProductImageRequest request);

    ProductImageResponse getProductImageById(Long id);

    List<ProductImageResponse> getImagesByVariant(Long productVariantId);

    void activateProductImage(Long id);

    void deactivateProductImage(Long id);

}