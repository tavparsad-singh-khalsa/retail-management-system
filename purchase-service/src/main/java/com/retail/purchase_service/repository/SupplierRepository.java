package com.retail.purchase_service.repository;

import com.retail.purchase_service.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Validation
    boolean existsByNameIgnoreCase(String name);

    boolean existsByPhone(String phone);

    boolean existsByGstNumber(String gstNumber);

    // Lookup
    Optional<Supplier> findByPhone(String phone);

    Optional<Supplier> findByGstNumber(String gstNumber);

    // Search
    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByIsActiveTrue();
}
