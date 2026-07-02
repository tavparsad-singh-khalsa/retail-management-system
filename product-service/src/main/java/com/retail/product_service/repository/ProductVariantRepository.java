package com.retail.product_service.repository;

import com.retail.product_service.entity.Product;
import com.retail.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant , Long > {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<ProductVariant> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<ProductVariant> findByProduct(Product product);

    List<ProductVariant> findByIsActiveTrue();
}
