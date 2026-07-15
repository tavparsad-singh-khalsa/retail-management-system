package com.retail.product_service.repository;

import com.retail.product_service.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand , Long> {

    Optional<Brand> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Brand> findByNameContainingIgnoreCase(String name);

    List<Brand> findByIsActiveTrue();

}
