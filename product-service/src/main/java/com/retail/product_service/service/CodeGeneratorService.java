package com.retail.product_service.service;

public interface CodeGeneratorService {
    String generateProductCode(Long productId);
    String generateSku(Long variantId);
    String generateBarcode(Long variantId);
}
