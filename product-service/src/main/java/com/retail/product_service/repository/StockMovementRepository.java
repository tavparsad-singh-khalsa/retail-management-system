package com.retail.product_service.repository;

import com.retail.product_service.entity.Inventory;
import com.retail.product_service.entity.StockMovement;
import com.retail.product_service.enums.MovementType;
import com.retail.product_service.enums.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // 1. Movement history of an inventory (newest first)
    List<StockMovement> findByInventoryOrderByCreatedAtDesc(Inventory inventory);

    // 2. Filter by movement type (e.g., All Purchases, All Sales)
    List<StockMovement> findByMovementType(MovementType movementType);

    // 3. Inventory + Movement Type (e.g., Only DAMAGE history for a specific variant)
    List<StockMovement> findByInventoryAndMovementType(Inventory inventory, MovementType movementType);

    // 4. Search by reference number (e.g., PO-2026-00015)
    Optional<StockMovement> findByReferenceNumber(String referenceNumber);

    // 5. Search by reference type (e.g., All Purchase Orders)
    List<StockMovement> findByReferenceType(ReferenceType referenceType);

    // ⭐ 6. Audit & Reporting: Filter movement history by date range
    List<StockMovement> findByInventoryAndCreatedAtBetween(Inventory inventory, LocalDateTime startDate, LocalDateTime endDate);
}