package com.retail.product_service.repository;

import com.retail.product_service.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute , Long>{

    Optional<Attribute> findByName(String name);

    boolean existsByName(String name);

    List<Attribute> findByNameContainingIgnoreCase(String name);
}
