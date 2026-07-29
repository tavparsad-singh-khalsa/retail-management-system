package com.retail.purchase_service.repository;

import com.retail.purchase_service.entity.Purchase;
import com.retail.purchase_service.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // Business Identifier
    boolean existsByPurchaseNumber(String purchaseNumber);

    Optional<Purchase> findByPurchaseNumber(String purchaseNumber);

    // Search & Filtering
    List<Purchase> findBySupplierId(Long supplierId);

    List<Purchase> findByStatus(PurchaseStatus status);

    List<Purchase> findByPurchaseDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // Combined Filter
    List<Purchase> findBySupplierIdAndStatus(
            Long supplierId,
            PurchaseStatus status
    );
}