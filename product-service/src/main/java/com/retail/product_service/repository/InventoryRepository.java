package com.retail.product_service.repository;

import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductVariant(ProductVariant productVariant);

    List<Inventory> findByCurrentStockLessThanEqual(Integer stock);

    List<Inventory> findByCurrentStock(Integer currentStock);

    List<Inventory> findByIsActiveTrue();

    boolean existsByProductVariant(ProductVariant productVariant);

    List<Inventory> findByIsActiveTrueOrderByProductVariantIdAsc();

    Optional<Inventory> findByIdAndIsActiveTrue(Long id);

    List<Inventory> findByIsActiveTrueAndCurrentStockLessThanEqual(Integer stock);
}