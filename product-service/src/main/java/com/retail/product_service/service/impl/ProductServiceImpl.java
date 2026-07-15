package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateProductRequest;
import com.retail.product_service.dto.request.UpdateProductRequest;
import com.retail.product_service.dto.response.ProductResponse;
import com.retail.product_service.entity.Brand;
import com.retail.product_service.entity.Category;
import com.retail.product_service.entity.Product;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.BrandRepository;
import com.retail.product_service.repository.CategoryRepository;
import com.retail.product_service.repository.ProductRepository;
import com.retail.product_service.service.CodeGeneratorService;
import com.retail.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CodeGeneratorService codeGeneratorService;

    @Transactional
    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ProductAlreadyExistsException("Product name already exists: " + request.getName());
        }

        // ⭐ Reads perfectly: Validate and get the category/brand
        Category category = validateAssignableCategory(request.getCategoryId());
        Brand brand = validateAssignableBrand(request.getBrandId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .hasVariants(false)
                .category(category)
                .brand(brand)
                .build();

        product = productRepository.save(product);
        String productCode = codeGeneratorService.generateProductCode(product.getId());
        product.setProductCode(productCode);

        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProduct(id);

        // 1. Name Changed? -> Duplicate Check
        if (request.getName() != null && !request.getName().equalsIgnoreCase(product.getName())) {
            if (productRepository.existsByNameIgnoreCase(request.getName())) {
                throw new ProductAlreadyExistsException("Product name already exists: " + request.getName());
            }
            product.setName(request.getName());
        }

        // 2. Update Standard Fields
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        // 3. Category Changed? -> Must exist, be active, AND be a leaf category
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category = validateAssignableCategory(request.getCategoryId());
            product.setCategory(category);
        }

        // 4. Brand Changed? -> Must exist and be active
        if (request.getBrandId() != null && !request.getBrandId().equals(product.getBrand().getId())) {
            Brand brand = validateAssignableBrand(request.getBrandId());
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return mapToResponse(getProduct(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductByProductCode(String productCode) {
        return mapToResponse(getProductByCode(productCode));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateProduct(Long id) {
        Product product = getProduct(id);

        if (product.getIsActive()) {
            return; // Idempotent
        }

        // ⭐ Enterprise Business Rule: We shouldn't activate a product if its parent category or brand is inactive!
        if (!product.getCategory().getIsActive()) {
            throw new CategoryInactiveException("Cannot activate product: Parent category is currently inactive.");
        }
        if (!product.getBrand().getIsActive()) {
            throw new BrandInactiveException("Cannot activate product: Associated brand is currently inactive.");
        }

        product.setIsActive(true);
        productRepository.save(product);
    }

    @Transactional
    @Override
    public void deactivateProduct(Long id) {
        Product product = getProduct(id);

        if (!product.getIsActive()) {
            return; // Idempotent
        }

        product.setIsActive(false);
        productRepository.save(product);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    // ⭐ IMPROVEMENT #1: Consistent lookup helper
    private Product getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with code: " + productCode));
    }

    // ⭐ FUTURE IMPROVEMENT APPLIED: Better Domain-Driven naming and Leaf validation
    private Category validateAssignableCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        if (!category.getIsActive()) {
            throw new CategoryInactiveException("Cannot assign an inactive category to a product.");
        }

        // ⭐ IMPROVEMENT #2: Prevent assigning products to root/parent categories
        if (categoryRepository.existsByParentCategory(category)) {
            throw new CategoryHierarchyException("Products can only be assigned to leaf categories (categories without subcategories).");
        }

        return category;
    }

    // ⭐ FUTURE IMPROVEMENT APPLIED: Renamed for consistency
    private Brand validateAssignableBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + brandId));

        if (!brand.getIsActive()) {
            throw new BrandInactiveException("Cannot assign an inactive brand to a product.");
        }
        return brand;
    }

    private Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        if (!category.getIsActive()) {
            throw new CategoryInactiveException("Cannot assign an inactive category to a product.");
        }
        return category;
    }

    private Brand getActiveBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with ID: " + brandId));

        if (!brand.getIsActive()) {
            throw new BrandInactiveException("Cannot assign an inactive brand to a product.");
        }
        return brand;
    }


    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .description(product.getDescription())
                .hasVariants(product.getHasVariants())
                .isActive(product.getIsActive())
                .categoryId(product.getCategory().getId())
                .brandId(product.getBrand().getId())
                .createdAt(product.getCreatedAt())
                .build();
    }


}
