package com.retail.product_service.repository;

import com.retail.product_service.entity.ProductImage;
import com.retail.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductVariant(ProductVariant productVariant);

    List<ProductImage> findByProductVariantAndIsActiveTrue(ProductVariant productVariant);

    List<ProductImage> findByProductVariantAndIsActiveTrueOrderByDisplayOrderAsc(
            ProductVariant productVariant
    );

    boolean existsByProductVariantAndIsPrimaryTrue(ProductVariant productVariant);

    boolean existsByProductVariantAndIsPrimaryTrueAndIsActiveTrue(
            ProductVariant productVariant
    );

    Optional<ProductImage> findByProductVariantAndIsPrimaryTrue(
            ProductVariant productVariant
    );

    Optional<ProductImage> findByProductVariantAndIsPrimaryTrueAndIsActiveTrue(
            ProductVariant productVariant
    );

    List<ProductImage> findByProductVariantOrderByDisplayOrderAsc(
            ProductVariant productVariant
    );

    // ⭐ New: Enforces active only and orders by display order for a base product
    List<ProductImage> findByProductVariant_ProductIdAndIsActiveTrueOrderByDisplayOrderAsc(Long productId);

    // ⭐ New: Enforces active only and orders by display order for a specific variant
    List<ProductImage> findByProductVariant_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long variantId);
}