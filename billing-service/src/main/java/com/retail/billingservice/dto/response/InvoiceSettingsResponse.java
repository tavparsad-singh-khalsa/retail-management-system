package com.retail.billingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettingsResponse {

    // ---- Shop Information ----
    private String shopName;
    private String address;
    private String phoneNumber;
    private String email;
    private String instagramUrl;
    private String googleMapsUrl;
    private String googleReviewUrl;

    // ---- Invoice Branding ----
    private String invoicePrefix;
    private String invoiceTagline;
    private String footerText;
    private Integer paymentTerms;
    private String currency;

    // ---- Display Toggles ----
    private Boolean showShopName;
    private Boolean showAddress;
    private Boolean showPhone;
    private Boolean showEmail;
    private Boolean showInstagram;
    private Boolean showGoogleMaps;
    private Boolean showGoogleReview;
    private Boolean showTagline;
    private Boolean showPaymentTerms;
    private Boolean showFooter;

    // ---- Tax Settings ----
    private Boolean taxEnabled;
    private String taxName;
    private BigDecimal taxRate;
    private Boolean showTax;
    private String gstin;
    private Boolean showGstin;

    // ---- Payment Settings ----
    private Boolean allowPartialPayment;

    private LocalDateTime updatedAt;

}