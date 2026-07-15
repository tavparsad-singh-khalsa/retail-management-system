package com.retail.product_service.repository;

import com.retail.product_service.entity.Brand;
import com.retail.product_service.entity.Category;
import com.retail.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    boolean existsByNameIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(Category category);

    List<Product> findByBrand(Brand brand);

    List<Product> findByCategoryAndBrand(Category category, Brand brand);

    List<Product> findByIsActiveTrue();
}