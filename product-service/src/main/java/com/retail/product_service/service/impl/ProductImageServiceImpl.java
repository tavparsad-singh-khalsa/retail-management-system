package com.retail.product_service.service.impl;

import com.retail.product_service.dto.request.CreateProductImageRequest;
import com.retail.product_service.dto.request.UpdateProductImageRequest;
import com.retail.product_service.dto.response.ProductImageResponse;
import com.retail.product_service.entity.Product;
import com.retail.product_service.entity.ProductImage;
import com.retail.product_service.entity.ProductVariant;
import com.retail.product_service.exception.*;
import com.retail.product_service.repository.ProductImageRepository;
import com.retail.product_service.repository.ProductVariantRepository;
import com.retail.product_service.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    @Override
    public ProductImageResponse createProductImage(CreateProductImageRequest request) {
        // 1. Validate Variant Hierarchy
        ProductVariant variant = getActiveVariantOrThrow(request.getProductVariantId());

        // 2. Safely Extract Default Values
        boolean isPrimary = Boolean.TRUE.equals(request.getIsPrimary());

        int displayOrder = Math.max(
                request.getDisplayOrder() == null ? 0 : request.getDisplayOrder(),
                0
        );

        // 3. Automatically make the first image primary (Defensive against inactive bad data)
        if (!productImageRepository.existsByProductVariantAndIsPrimaryTrueAndIsActiveTrue(variant)) {
            isPrimary = true;
        }

        // 4. Demote existing primary (relies on JPA dirty checking)
        if (isPrimary) {
            demoteOtherPrimaryImages(variant, null);
        }

        // 5. Create the New Image Entity
        ProductImage productImage = ProductImage.builder()
                .productVariant(variant)
                .imageUrl(request.getImageUrl())
                .isPrimary(isPrimary)
                .displayOrder(displayOrder)
                .build();

        // 6. Save and Map Response
        return mapToResponse(productImageRepository.save(productImage));
    }

    @Transactional
    @Override
    public ProductImageResponse updateProductImage(Long id, UpdateProductImageRequest request) {
        // Protected against editing inactive images
        ProductImage image = getActiveProductImageOrThrow(id);
        ProductVariant variant = image.getProductVariant();

        // 1. Image URL Changed?
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            image.setImageUrl(request.getImageUrl().trim());
        }

        // 2. Primary Changed?
        if (request.getIsPrimary() != null) {
            boolean requestedPrimary = request.getIsPrimary();
            boolean isCurrentlyPrimary = image.getIsPrimary();

            if (requestedPrimary && !isCurrentlyPrimary) {
                // Demote Existing Primary & Promote Current
                demoteOtherPrimaryImages(variant, image.getId());
                image.setIsPrimary(true);

            } else if (!requestedPrimary && isCurrentlyPrimary) {
                // CORE BUSINESS RULE: Never allow a variant to end up with zero primary images.
                throw new InvalidProductImageException(
                        "Cannot remove primary status. Please set another image as primary instead."
                );
            }
        }

        // 3. Update Display Order
        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(Math.max(0, request.getDisplayOrder()));
        }

        // 4. Save & Response
        return mapToResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getProductImageById(Long id) {
        // Ensures we don't return inactive images by ID unless explicitly required
        return mapToResponse(getActiveProductImageOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByVariant(Long productVariantId) {
        // Reuse our powerful hierarchical validation
        ProductVariant variant = getActiveVariantOrThrow(productVariantId);

        // Guarantees frontend always receives images in display order
        return productImageRepository.findByProductVariantAndIsActiveTrueOrderByDisplayOrderAsc(variant).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public void activateProductImage(Long id) {
        // We use getProductImageOrThrow here because the image is likely inactive!
        ProductImage image = getProductImageOrThrow(id);

        if (image.getIsActive()) return;

        // Ensure parent hierarchy is completely active before allowing activation
        getActiveVariantOrThrow(image.getProductVariant().getId());

        // Guarantee only one primary image during activation
        if (image.getIsPrimary()) {
            demoteOtherPrimaryImages(image.getProductVariant(), image.getId());
        }

        image.setIsActive(true);
        productImageRepository.save(image);
    }

    @Transactional
    @Override
    public void deactivateProductImage(Long id) {
        ProductImage image = getProductImageOrThrow(id);

        if (!image.getIsActive()) return;

        // CORE BUSINESS RULE EXTENSION: You cannot deactivate the primary image
        if (image.getIsPrimary()) {
            throw new InvalidProductImageException(
                    "Cannot deactivate the primary image. Please set another image as primary first."
            );
        }

        image.setIsActive(false);
        productImageRepository.save(image);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private ProductImage getProductImageOrThrow(Long id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> new ProductImageNotFoundException("Product Image not found with ID: " + id));
    }

    private ProductImage getActiveProductImageOrThrow(Long id) {
        ProductImage image = getProductImageOrThrow(id);

        if (!image.getIsActive()) {
            throw new ProductImageInactiveException("Product image is inactive.");
        }

        return image;
    }

    private ProductVariant getActiveVariantOrThrow(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product Variant not found with ID: " + id));

        if (!variant.getIsActive()) {
            throw new ProductInactiveException("Cannot use an inactive variant.");
        }

        validateParentProductHierarchy(variant.getProduct());

        return variant;
    }

    // Fixes the 17-line duplication warning across Product & Variant Services
    private void validateParentProductHierarchy(Product product) {
        if (!product.getIsActive()) {
            throw new ProductInactiveException("Cannot use variant: Parent product is inactive.");
        }
        if (!product.getCategory().getIsActive()) {
            throw new CategoryInactiveException("Cannot use variant: Parent category is inactive.");
        }
        if (!product.getBrand().getIsActive()) {
            throw new BrandInactiveException("Cannot use variant: Parent brand is inactive.");
        }
    }

    // Centralizes the primary image demotion logic used in Create, Update, and Activate
    private void demoteOtherPrimaryImages(ProductVariant variant, Long currentImageId) {
        productImageRepository.findByProductVariantAndIsPrimaryTrueAndIsActiveTrue(variant)
                .ifPresent(existing -> {
                    // ⭐ FIXED: Removed the unnecessary 'currentImageId == null' check.
                    // In Java, object.equals(null) safely evaluates to false.
                    if (!existing.getId().equals(currentImageId)) {
                        existing.setIsPrimary(false);
                    }
                });
    }

    private ProductImageResponse mapToResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .productId(image.getProductVariant().getProduct().getId())
                .productName(image.getProductVariant().getProduct().getName())
                .productVariantId(image.getProductVariant().getId())
                .sku(image.getProductVariant().getSku())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .isActive(image.getIsActive())
                .build();
    }
}