package com.retail.product_service.repository;

import com.retail.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category , Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByParentCategory(Category parentCategory);

    List<Category> findByNameContainingIgnoreCase(String name);
}
