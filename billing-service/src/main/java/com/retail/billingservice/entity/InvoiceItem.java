package com.retail.billingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    private Long productId;
    private Long variantId;

    private String sku;
    private String barcode;

    private String productName;
    private String variantName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;
}
