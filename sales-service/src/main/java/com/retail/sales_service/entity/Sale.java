package com.retail.sales_service.entity;

import com.retail.sales_service.enums.PaymentStatus;
import com.retail.sales_service.enums.SaleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique human-readable sale number (generated later)
    @Column(unique = true)
    private String saleNumber;

    // Client-supplied idempotency key covering the entire create-sale operation.
    // Nullable for backward compatibility; UNIQUE index protects concurrent duplicate requests.
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    // Deterministic SHA-256 fingerprint of the normalized CreateSaleRequest.
    @Column(name = "idempotency_request_hash", length = 64)
    private String idempotencyRequestHash;

    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus saleStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private LocalDateTime saleDate;

    @Column(length = 1000)
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // Prepared relationships (SaleItem and Payment not created yet)
    @Builder.Default
    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> saleItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.saleDate == null) {
            this.saleDate = LocalDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
    }
}
