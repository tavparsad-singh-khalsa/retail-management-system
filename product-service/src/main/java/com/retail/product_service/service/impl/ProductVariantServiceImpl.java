package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateProductVariantRequest;
import com.retail.product_service.dto.request.UpdateProductVariantRequest;
import com.retail.product_service.dto.response.ProductVariantResponse;
import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.Product;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.InventoryRepository;
import com.retail.product_service.repository.ProductRepository;
import com.retail.product_service.repository.ProductVariantRepository;
import com.retail.product_service.service.CodeGeneratorService;
import com.retail.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final InventoryRepository inventoryRepository;

    @Transactional
    @Override
    public ProductVariantResponse createProductVariant(CreateProductVariantRequest request) {
        // 1. Validate Product
        Product product = getActiveProductOrThrow(request.getProductId());

        // 2. Validate Pricing
        validatePricing(request.getPurchasePrice(), request.getMinimumSellingPrice(), request.getSellingPrice());

        // 3. Create and Save Variant (Initial Save to generate ID)
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .purchasePrice(request.getPurchasePrice())
                .minimumSellingPrice(request.getMinimumSellingPrice())
                .sellingPrice(request.getSellingPrice())
                // .isActive(true) is removed; handled by BaseEntity defaults
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);

        // 4. Generate SKU & Barcode automatically
        savedVariant.setSku(codeGeneratorService.generateSku(savedVariant.getId()));
        savedVariant.setBarcode(codeGeneratorService.generateBarcode(savedVariant.getId()));

        // 5. Save Variant again with generated codes
        savedVariant = productVariantRepository.save(savedVariant);

        // 6. Automatic Inventory Creation (Idempotent approach)
        if (!inventoryRepository.existsByProductVariant(savedVariant)) {
            Inventory initialInventory = Inventory.builder()
                    .productVariant(savedVariant)
                    .currentStock(0)
                    .reservedStock(0)
                    .minimumStock(0)
                    .maximumStock(null)
                    .reorderLevel(0)
                    .build();

            inventoryRepository.save(initialInventory);
        }

        // 7. Update product's hasVariants flag if needed
        if (product.getHasVariants() == null || !product.getHasVariants()) {
            product.setHasVariants(true);
            productRepository.save(product);
        }

        // 8. Return Response
        return mapToResponse(savedVariant);
    }

    @Transactional
    @Override
    public ProductVariantResponse updateProductVariant(Long id, UpdateProductVariantRequest request) {
        ProductVariant variant = getProductVariantOrThrow(id);

        // 1. Validate New Prices (if they are being updated)
        BigDecimal purchasePrice = request.getPurchasePrice() != null ? request.getPurchasePrice() : variant.getPurchasePrice();
        BigDecimal minSellingPrice = request.getMinimumSellingPrice() != null ? request.getMinimumSellingPrice() : variant.getMinimumSellingPrice();
        BigDecimal sellingPrice = request.getSellingPrice() != null ? request.getSellingPrice() : variant.getSellingPrice();

        validatePricing(purchasePrice, minSellingPrice, sellingPrice);

        // 2. Update Fields
        if (request.getPurchasePrice() != null) variant.setPurchasePrice(request.getPurchasePrice());
        if (request.getMinimumSellingPrice() != null) variant.setMinimumSellingPrice(request.getMinimumSellingPrice());
        if (request.getSellingPrice() != null) variant.setSellingPrice(request.getSellingPrice());

        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return mapToResponse(updatedVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long id) {
        return mapToResponse(getProductVariantOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantBySku(String sku) {
        ProductVariant variant = productVariantRepository.findBySku(sku)
                .orElseThrow(() -> new ProductVariantNotFoundException("Variant not found with SKU: " + sku));

        // Prevent leaking inactive variants
        if (!variant.getIsActive()) {
            throw new ProductInactiveException("Product Variant is inactive.");
        }

        return mapToResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        Product product = getActiveProductOrThrow(productId);

        return productVariantRepository.findByProductAndIsActiveTrue(product).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateVariant(Long id) {
        ProductVariant variant = getProductVariantOrThrow(id);

        if (variant.getIsActive()) return;

        // Enterprise Rule: Do not activate if the parent product hierarchy is inactive
        getActiveProductOrThrow(variant.getProduct().getId());

        variant.setIsActive(true);
        productVariantRepository.save(variant);
    }

    @Transactional
    @Override
    public void deactivateVariant(Long id) {
        ProductVariant variant = getProductVariantOrThrow(id);

        if (!variant.getIsActive()) return;

        variant.setIsActive(false);
        productVariantRepository.save(variant);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private ProductVariant getProductVariantOrThrow(Long id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with ID: " + id));
    }

    private Product getActiveProductOrThrow(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (!product.getIsActive()) {
            throw new ProductInactiveException("Cannot use an inactive product.");
        }
        if (!product.getCategory().getIsActive()) {
            throw new CategoryInactiveException("Cannot use product: Parent category is inactive.");
        }
        if (!product.getBrand().getIsActive()) {
            throw new BrandInactiveException("Cannot use product: Parent brand is inactive.");
        }
        return product;
    }

    private void validatePricing(BigDecimal purchasePrice, BigDecimal minimumSellingPrice, BigDecimal sellingPrice) {
        if (purchasePrice == null || minimumSellingPrice == null || sellingPrice == null) {
            throw new InvalidPriceException("Prices cannot be null.");
        }
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Purchase price cannot be negative.");
        }
        if (sellingPrice.compareTo(purchasePrice) < 0) {
            throw new InvalidPriceException("Selling price cannot be less than purchase price.");
        }
        if (minimumSellingPrice.compareTo(purchasePrice) < 0) {
            throw new InvalidPriceException("Minimum selling price cannot be less than purchase price.");
        }
        if (minimumSellingPrice.compareTo(sellingPrice) > 0) {
            throw new InvalidPriceException("Minimum selling price cannot be greater than selling price.");
        }
    }

    private ProductVariantResponse mapToResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                // V2 Enhancement: Include productName, categoryName and brandName
                // TODO: .productName(variant.getProduct().getName())
                // TODO: .categoryName(variant.getProduct().getCategory().getName())
                // TODO: .brandName(variant.getProduct().getBrand().getName())
                .sku(variant.getSku())
                .barcode(variant.getBarcode())
                .purchasePrice(variant.getPurchasePrice())
                .minimumSellingPrice(variant.getMinimumSellingPrice())
                .sellingPrice(variant.getSellingPrice())
                .isActive(variant.getIsActive())
                .createdAt(variant.getCreatedAt())
                .build();
    }
}