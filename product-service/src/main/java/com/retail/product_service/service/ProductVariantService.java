package com.retail.product_service.service;

import com.retail.product_service.dto.request.CreateProductVariantRequest;
import com.retail.product_service.dto.request.UpdateProductVariantRequest;
import com.retail.product_service.dto.response.ProductVariantResponse;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponse createProductVariant(CreateProductVariantRequest request);

    ProductVariantResponse updateProductVariant(Long id, UpdateProductVariantRequest request);

    ProductVariantResponse getVariantById(Long id);

    ProductVariantResponse getVariantBySku(String sku);

    List<ProductVariantResponse> getVariantsByProduct(Long productId);

    void activateVariant(Long id);

    void deactivateVariant(Long id);

    List<ProductVariantResponse> getAllVariants();

    ProductVariantResponse getVariantByBarcode(String barcode);
}
