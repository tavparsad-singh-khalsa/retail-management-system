package com.retail.billingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettings {

    @Id
    private Long id;

    // ---- Shop Information ----
    @Builder.Default
    @Column(nullable = false, length = 255)
    private String shopName = "";

    @Builder.Default
    @Column(nullable = false, length = 500)
    private String address = "";

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String phoneNumber = "";

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String instagramUrl;

    @Column(length = 500)
    private String googleMapsUrl;

    @Column(length = 500)
    private String googleReviewUrl;

    // ---- Invoice Branding ----
    @Builder.Default
    @Column(nullable = false, length = 10)
    private String invoicePrefix = "INV";

    @Builder.Default
    @Column(nullable = false, length = 255)
    private String invoiceTagline = "";

    @Builder.Default
    @Column(nullable = false, length = 500)
    private String footerText = "";

    @Builder.Default
    @Column(nullable = false)
    private Integer paymentTerms = 30;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    // ---- Display Toggles ----
    @Builder.Default
    @Column(nullable = false)
    private Boolean showShopName = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showAddress = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showPhone = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showEmail = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showInstagram = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showGoogleMaps = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showGoogleReview = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showTagline = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showPaymentTerms = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showFooter = true;

    // ---- Tax Settings ----
    @Builder.Default
    @Column(nullable = false)
    private Boolean taxEnabled = false;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String taxName = "GST";

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showTax = false;

    @Column(length = 30)
    private String gstin;

    @Builder.Default
    @Column(nullable = false)
    private Boolean showGstin = false;

    // ---- Payment Settings ----
    @Builder.Default
    @Column(nullable = false)
    private Boolean allowPartialPayment = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}