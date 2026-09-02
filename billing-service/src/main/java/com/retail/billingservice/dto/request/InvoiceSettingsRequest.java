package com.retail.billingservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSettingsRequest {

    // ---- Shop Information ----
    @NotBlank
    @Size(max = 255)
    private String shopName;

    @NotBlank
    @Size(max = 500)
    private String address;

    @NotBlank
    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String instagramUrl;

    @Size(max = 500)
    private String googleMapsUrl;

    @Size(max = 500)
    private String googleReviewUrl;

    // ---- Invoice Branding ----
    @NotBlank
    @Size(max = 10)
    private String invoicePrefix;

    @Size(max = 255)
    private String invoiceTagline;

    @Size(max = 500)
    private String footerText;

    @NotNull
    @Min(0)
    @Max(365)
    private Integer paymentTerms;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    // ---- Display Toggles ----
    @NotNull
    private Boolean showShopName;
    @NotNull
    private Boolean showAddress;
    @NotNull
    private Boolean showPhone;
    @NotNull
    private Boolean showEmail;
    @NotNull
    private Boolean showInstagram;
    @NotNull
    private Boolean showGoogleMaps;
    @NotNull
    private Boolean showGoogleReview;
    @NotNull
    private Boolean showTagline;
    @NotNull
    private Boolean showPaymentTerms;
    @NotNull
    private Boolean showFooter;

    // ---- Tax Settings ----
    @NotNull
    private Boolean taxEnabled;

    @NotBlank
    @Size(max = 50)
    private String taxName;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal taxRate;

    @NotNull
    private Boolean showTax;

    @Size(max = 30)
    private String gstin;

    @NotNull
    private Boolean showGstin;

    // ---- Payment Settings ----
    @NotNull
    private Boolean allowPartialPayment;

}