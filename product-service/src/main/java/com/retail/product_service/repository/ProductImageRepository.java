package com.retail.product_service.repository;

import com.retail.product_service.entity.Product;
import com.retail.product_service.entity.ProductImage;
import com.retail.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage , Long> {

    List<ProductImage> findByProduct(Product product);

    List<ProductImage> findByProductVariant(ProductVariant productVariant);

    List<ProductImage> findByProductOrderByDisplayOrderAsc(Product product);
}
