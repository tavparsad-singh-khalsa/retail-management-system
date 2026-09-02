package com.retail.purchase_service.service.interfaces;

import com.retail.purchase_service.dto.request.CreatePurchaseRequest;
import com.retail.purchase_service.dto.response.PurchaseResponse;
import com.retail.purchase_service.enums.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {

    // Create Operation
    PurchaseResponse createPurchase(CreatePurchaseRequest request);

    // Read Operations
    PurchaseResponse getPurchaseById(Long id);
    PurchaseResponse getPurchaseByNumber(String purchaseNumber);
    Page<PurchaseResponse> getAllPurchases(Pageable pageable);

    // Search & Filtering Operations
    List<PurchaseResponse> getPurchasesByStatus(PurchaseStatus status);
    List<PurchaseResponse> getPurchasesBySupplier(Long supplierId);
    List<PurchaseResponse> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate);
    List<PurchaseResponse> getPurchasesBySupplierAndStatus(Long supplierId, PurchaseStatus status);

    // Lifecycle Operations
    PurchaseResponse approvePurchase(Long id);
    PurchaseResponse receivePurchase(Long id);
    PurchaseResponse cancelPurchase(Long id);
}