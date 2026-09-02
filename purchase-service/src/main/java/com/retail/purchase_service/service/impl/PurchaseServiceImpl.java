package com.retail.purchase_service.service.impl;

import com.retail.purchase_service.client.InventoryClient;
import com.retail.purchase_service.dto.integration.request.InventoryReceiveItemRequest;
import com.retail.purchase_service.dto.integration.request.InventoryReceiveRequest;
import com.retail.purchase_service.dto.integration.response.InventoryOperationResponse;
import com.retail.purchase_service.dto.request.CreatePurchaseRequest;
import com.retail.purchase_service.dto.request.PurchaseItemRequest;
import com.retail.purchase_service.dto.response.PurchaseItemResponse;
import com.retail.purchase_service.dto.response.PurchaseResponse;
import com.retail.purchase_service.entity.Purchase;
import com.retail.purchase_service.entity.PurchaseItem;
import com.retail.purchase_service.entity.Supplier;
import com.retail.purchase_service.enums.PurchaseStatus;
import com.retail.purchase_service.exception.*;
import com.retail.purchase_service.repository.PurchaseRepository;
import com.retail.purchase_service.repository.SupplierRepository;
import com.retail.purchase_service.service.interfaces.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;

    // Kept as-is for V1. Will migrate to SupplierService later.
    private final SupplierRepository supplierRepository;

    private final InventoryClient inventoryClient;

    @Override
    @Transactional
    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {
        // High-level workflow orchestration
        validateDuplicateVariants(request.getItems());
        Supplier supplier = validateSupplier(request.getSupplierId());
        String poNumber = generatePurchaseNumber();

        Purchase purchase = mapToEntity(request, supplier, poNumber);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(savedPurchase);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(Long id) {
        Purchase purchase = getPurchaseEntity(id);
        return mapToResponse(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseByNumber(String purchaseNumber) {
        Purchase purchase = purchaseRepository.findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new PurchaseNotFoundException(
                        "Purchase not found with purchase number: " + purchaseNumber));
        return mapToResponse(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseResponse> getAllPurchases(Pageable pageable) {
        return purchaseRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByStatus(PurchaseStatus status) {
        return purchaseRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + supplierId);
        }

        return purchaseRepository.findBySupplierId(supplierId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date cannot be after end date.");
        }

        return purchaseRepository.findByPurchaseDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesBySupplierAndStatus(Long supplierId, PurchaseStatus status) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + supplierId);
        }

        return purchaseRepository.findBySupplierIdAndStatus(supplierId, status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public PurchaseResponse approvePurchase(Long id) {
        Purchase purchase = getPurchaseEntity(id);

        if (purchase.getStatus() != PurchaseStatus.DRAFT) {
            throw new InvalidPurchaseStateException(
                    "Only DRAFT purchases can be approved. Current status: " + purchase.getStatus());
        }

        purchase.setStatus(PurchaseStatus.APPROVED);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(savedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse receivePurchase(Long id) {

        // 1. Validation (Using helper method to avoid duplication)
        Purchase purchase = getPurchaseEntity(id);

        if (purchase.getStatus() != PurchaseStatus.APPROVED) {
            throw new InvalidPurchaseStateException(
                    "Cannot receive purchase. Expected status APPROVED, but was " + purchase.getStatus());
        }

        log.info("Initiating receive process for Purchase: {}", purchase.getPurchaseNumber());

        // 2. Map the Request
        List<InventoryReceiveItemRequest> itemRequests = purchase.getItems().stream()
                .map(item -> InventoryReceiveItemRequest.builder()
                        .productVariantId(item.getProductVariantId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        InventoryReceiveRequest inventoryRequest = InventoryReceiveRequest.builder()
                .referenceNumber(purchase.getPurchaseNumber())
                .items(itemRequests)
                .build();

        // 3. Call the Product Service
        InventoryOperationResponse response;
        try {
            log.info("Sending inventory update to Product Service for purchase {}", purchase.getPurchaseNumber());
            response = inventoryClient.receiveInventory(inventoryRequest);
        } catch (Exception e) {
            // Translating potential Feign/Rest connection errors into a domain exception
            log.error("Network or execution error calling Product Service for Purchase: {}", purchase.getPurchaseNumber(), e);
            throw new InventoryUpdateException("Failed to communicate with Product Service: " + e.getMessage());
        }

        // 4. Handle the Response safely to prevent NullPointerException
        if (response == null) {
            throw new InventoryUpdateException("No response received from Product Service.");
        }

        if (!response.isSuccess()) {
            log.error("Product service failed to update inventory for Purchase: {}. Reason: {}",
                    purchase.getPurchaseNumber(), response.getMessage());
            throw new InventoryUpdateException("Failed to update inventory: " + response.getMessage());
        }

        // 5. Update and Save
        // A RuntimeException thrown above guarantees the local database transaction rolls back,
        // and the purchase status will not be updated to the RECEIVED state.
        purchase.setStatus(PurchaseStatus.RECEIVED);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        log.info("Purchase {} successfully marked as RECEIVED. Inventory updated. Transaction ID: {}",
                savedPurchase.getPurchaseNumber(), response.getTransactionId());

        return mapToResponse(savedPurchase);
    }

    @Override
    @Transactional
    public PurchaseResponse cancelPurchase(Long id) {
        Purchase purchase = getPurchaseEntity(id);

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            throw new InvalidPurchaseStateException("Cannot cancel a purchase that has already been RECEIVED.");
        }

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new InvalidPurchaseStateException("Purchase is already CANCELLED.");
        }

        purchase.setStatus(PurchaseStatus.CANCELLED);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        return mapToResponse(savedPurchase);
    }

    // --- New Helper Method ---

    private Purchase getPurchaseEntity(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new PurchaseNotFoundException("Purchase not found with id: " + id));
    }

    // --- Validation Methods ---

    private void validateDuplicateVariants(List<PurchaseItemRequest> items) {
        Set<Long> uniqueVariantIds = new HashSet<>();
        for (PurchaseItemRequest item : items) {
            if (!uniqueVariantIds.add(item.getProductVariantId())) {
                throw new DuplicateProductVariantException(
                        "Duplicate product variant ID found in request: " + item.getProductVariantId());
            }
        }
    }

    private Supplier validateSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Supplier not found with id: " + supplierId));

        if (!supplier.getIsActive()) {
            throw new InactiveSupplierException("Cannot create purchase order for an inactive supplier: " + supplier.getName());
        }

        return supplier;
    }

    // --- Entity Construction Methods ---

    private Purchase mapToEntity(CreatePurchaseRequest request, Supplier supplier, String poNumber) {
        Purchase purchase = Purchase.builder()
                .purchaseNumber(poNumber)
                .supplier(supplier)
                .purchaseDate(request.getPurchaseDate())
                .remarks(safeTrim(request.getRemarks()))
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseItemRequest itemRequest : request.getItems()) {
            PurchaseItem item = PurchaseItem.builder()
                    .productVariantId(itemRequest.getProductVariantId())
                    .quantity(itemRequest.getQuantity())
                    .purchasePrice(itemRequest.getPurchasePrice())
                    .build();

            purchase.addItem(item);
            totalAmount = totalAmount.add(calculateLineTotal(item));
        }

        purchase.setTotalAmount(totalAmount);

        return purchase;
    }

    private BigDecimal calculateLineTotal(PurchaseItem item) {
        return item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private String generatePurchaseNumber() {
        int currentYear = LocalDate.now().getYear();
        String poNumber;

        // Loop ensures absolute uniqueness against the database
        do {
            String shortUuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            poNumber = String.format("PO-%d-%s", currentYear, shortUuid);
        } while (purchaseRepository.existsByPurchaseNumber(poNumber));

        return poNumber;
    }

    private String safeTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // --- Mapping Methods ---

    private PurchaseResponse mapToResponse(Purchase purchase) {
        List<PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                .map(item -> PurchaseItemResponse.builder()
                        .id(item.getId())
                        .productVariantId(item.getProductVariantId())
                        .quantity(item.getQuantity())
                        .purchasePrice(item.getPurchasePrice())
                        .build())
                .toList();

        return PurchaseResponse.builder()
                .id(purchase.getId())
                .purchaseNumber(purchase.getPurchaseNumber())
                .supplierId(purchase.getSupplier().getId())
                .supplierName(purchase.getSupplier().getName())
                .status(purchase.getStatus())
                .purchaseDate(purchase.getPurchaseDate())
                .remarks(purchase.getRemarks())
                .totalAmount(purchase.getTotalAmount())
                .items(itemResponses)
                .isActive(purchase.getIsActive())
                .build();
    }
}