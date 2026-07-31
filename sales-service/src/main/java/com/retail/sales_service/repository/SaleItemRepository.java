package com.retail.sales_service.repository;

import com.retail.sales_service.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);
}

