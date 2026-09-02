package com.retail.billingservice.security;

import com.retail.billingservice.entity.InvoiceSettings;
import com.retail.billingservice.repository.InvoiceSettingsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvoiceSettingsSecurityTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private InvoiceSettingsRepository repository;
    @Value("${jwt.secret}") private String secret;

    private static final String VALID_BODY = """
            {
              "shopName": "Owner Shop",
              "address": "MG Road",
              "phoneNumber": "+91 90000 00000",
              "email": "",
              "instagramUrl": "",
              "googleMapsUrl": "",
              "googleReviewUrl": "",
              "invoicePrefix": "INV",
              "invoiceTagline": "",
              "footerText": "",
              "paymentTerms": 30,
              "currency": "INR",
              "showShopName": true,
              "showAddress": true,
              "showPhone": true,
              "showEmail": true,
              "showInstagram": true,
              "showGoogleMaps": true,
              "showGoogleReview": true,
              "showTagline": true,
              "showPaymentTerms": true,
              "showFooter": true,
              "taxEnabled": false,
              "taxName": "GST",
              "taxRate": 0,
              "showTax": false,
              "gstin": "",
              "showGstin": false,
              "allowPartialPayment": true
            }
            """;

    private void seedSettingsRow() {
        repository.save(InvoiceSettings.builder().id(1L).build());
    }

    private String token(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000));
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(key).compact();
    }

    private String bearer(String username, String role) {
        return "Bearer " + token(username, role);
    }

    @Test
    void getSettingsRejectedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/invoice-settings"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSettingsAllowedForAnyAuthenticatedUser() throws Exception {
        seedSettingsRow();

        mockMvc.perform(get("/api/v1/invoice-settings")
                        .header("Authorization", bearer("cashier", "CASHIER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoicePrefix").value("INV"))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    void putSettingsRejectedWithoutToken() throws Exception {
        mockMvc.perform(put("/api/v1/invoice-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void putSettingsRejectedForNonOwner() throws Exception {
        seedSettingsRow();

        mockMvc.perform(put("/api/v1/invoice-settings")
                        .header("Authorization", bearer("cashier", "CASHIER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void putSettingsRejectedWhenRoleClaimMissing() throws Exception {
        seedSettingsRow();

        mockMvc.perform(put("/api/v1/invoice-settings")
                        .header("Authorization", bearer("owner", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void putSettingsAllowedForOwnerAndPersists() throws Exception {
        seedSettingsRow();

        mockMvc.perform(put("/api/v1/invoice-settings")
                        .header("Authorization", bearer("owner", "OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopName").value("Owner Shop"))
                .andExpect(jsonPath("$.paymentTerms").value(30))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.allowPartialPayment").value(true));

        com.retail.billingservice.entity.InvoiceSettings persisted =
                repository.findById(1L).orElseThrow();
        assertEquals("Owner Shop", persisted.getShopName());
        assertEquals(true, persisted.getAllowPartialPayment());
    }
}