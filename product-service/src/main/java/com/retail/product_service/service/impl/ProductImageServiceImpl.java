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
        ProductVariant variant = getActiveVariantOrThrow(request.getProductVariantId());

        boolean isPrimary = Boolean.TRUE.equals(request.getIsPrimary());
        int displayOrder = Math.max(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder(), 0);

        if (!productImageRepository.existsByProductVariantAndIsPrimaryTrueAndIsActiveTrue(variant)) {
            isPrimary = true;
        }

        if (isPrimary) {
            demoteOtherPrimaryImages(variant, null);
        }

        ProductImage productImage = ProductImage.builder()
                .productVariant(variant)
                .imageUrl(request.getImageUrl())
                .isPrimary(isPrimary)
                .displayOrder(displayOrder)
                .build();

        return mapToResponse(productImageRepository.save(productImage));
    }

    @Transactional
    @Override
    public ProductImageResponse updateProductImage(Long id, UpdateProductImageRequest request) {
        ProductImage image = getActiveProductImageOrThrow(id);
        ProductVariant variant = image.getProductVariant();

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            image.setImageUrl(request.getImageUrl().trim());
        }

        if (request.getIsPrimary() != null) {
            boolean requestedPrimary = request.getIsPrimary();
            boolean isCurrentlyPrimary = image.getIsPrimary();

            if (requestedPrimary && !isCurrentlyPrimary) {
                demoteOtherPrimaryImages(variant, image.getId());
                image.setIsPrimary(true);
            } else if (!requestedPrimary && isCurrentlyPrimary) {
                throw new InvalidProductImageException(
                        "Cannot remove primary status. Please set another image as primary instead."
                );
            }
        }

        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(Math.max(0, request.getDisplayOrder()));
        }

        return mapToResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getProductImageById(Long id) {
        return mapToResponse(getActiveProductImageOrThrow(id));
    }

    // ⭐ FIXED: Implemented using the new safe repository method
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImagesByProductId(Long productId) {
        return productImageRepository.findByProductVariant_ProductIdAndIsActiveTrueOrderByDisplayOrderAsc(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ⭐ FIXED: Renamed from getImagesByVariant and implemented fully
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImagesByVariantId(Long variantId) {
        getActiveVariantOrThrow(variantId); // Validate hierarchy first
        return productImageRepository.findByProductVariant_IdAndIsActiveTrueOrderByDisplayOrderAsc(variantId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ⭐ FIXED: Implemented business logic for setting the primary image
    @Transactional
    @Override
    public void setPrimaryImage(Long id) {
        ProductImage image = getActiveProductImageOrThrow(id);

        if (image.getIsPrimary()) {
            return; // Idempotent operation
        }

        demoteOtherPrimaryImages(image.getProductVariant(), image.getId());
        image.setIsPrimary(true);
        productImageRepository.save(image);
    }

    @Transactional
    @Override
    public void activateProductImage(Long id) {
        ProductImage image = getProductImageOrThrow(id);

        if (image.getIsActive()) return;

        getActiveVariantOrThrow(image.getProductVariant().getId());

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

    private void demoteOtherPrimaryImages(ProductVariant variant, Long currentImageId) {
        productImageRepository.findByProductVariantAndIsPrimaryTrueAndIsActiveTrue(variant)
                .ifPresent(existing -> {
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