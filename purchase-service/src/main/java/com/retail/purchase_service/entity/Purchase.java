package com.retail.purchase_service.entity;

import com.retail.purchase_service.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Purchase extends BaseEntity {

    // The human-readable business identifier (e.g., PO-2026-000001)
    @Column(nullable = false, unique = true, length = 50)
    private String purchaseNumber;

    // Standard practice: ManyToOne should be LAZY to prevent N+1 query performance issues
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status = PurchaseStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(length = 500)
    private String remarks;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Cascade ALL and orphanRemoval ensures that if we remove an item from the list, it gets deleted from the DB
    @Builder.Default
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<PurchaseItem> items = new ArrayList<>();

    // --- Bi-directional Relationship Helper Methods ---

    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
    }

    public void removeItem(PurchaseItem item) {
        items.remove(item);
        item.setPurchase(null);
    }
}