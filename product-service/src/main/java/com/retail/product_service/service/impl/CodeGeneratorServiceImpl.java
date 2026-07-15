package com.retail.product_service.service.impl;

import com.retail.product_service.service.CodeGeneratorService;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    @Override
    public String generateProductCode(Long productId) {
        int currentYear = Year.now().getValue();
        return String.format("PRD-%d-%06d", currentYear, productId);
    }

    @Override
    public String generateSku(Long variantId) {
        int currentYear = Year.now().getValue();
        return String.format("SKU-%d-%06d", currentYear, variantId);
    }

    @Override
    public String generateBarcode(Long variantId) {
        int currentYear = Year.now().getValue();
        return String.format("BAR-%d-%06d", currentYear, variantId);
    }
}
