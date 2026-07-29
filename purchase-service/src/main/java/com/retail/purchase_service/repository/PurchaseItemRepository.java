package com.retail.purchase_service.repository;

import com.retail.purchase_service.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository
        extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findByPurchaseId(Long purchaseId);

    List<PurchaseItem> findByProductVariantId(Long productVariantId);
}