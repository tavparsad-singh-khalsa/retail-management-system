package com.retail.sales_service.repository;

import com.retail.sales_service.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findBySaleNumber(String saleNumber);

    boolean existsBySaleNumber(String saleNumber);
}
