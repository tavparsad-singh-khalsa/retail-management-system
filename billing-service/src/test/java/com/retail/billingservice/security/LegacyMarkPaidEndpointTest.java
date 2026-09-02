package com.retail.billingservice.security;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guards the removal of the legacy invoice-level mark-paid endpoint (audit D2).
 *
 * <p>The Sale + Payment records in the sales-service are the single source of
 * truth for payment settlement (SaleService.settlePayment). The old
 * PATCH /api/v1/invoices/{id}/mark-paid mutated only the invoice snapshot and
 * could mark an invoice PAID while its sale stayed PARTIALLY_PAID. It is no
 * longer used by any caller and was removed; a partially-paid sale therefore
 * cannot be made invoice-PAID through a legacy shortcut anymore.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LegacyMarkPaidEndpointTest {

    @Autowired private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String secret;

    private String bearer() {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("owner")
                .claim("role", "OWNER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void legacyMarkPaidEndpointIsRemoved() throws Exception {
        // A partially-paid sale must never be payable through the legacy path;
        // after removal the endpoint simply does not exist (404).
        mockMvc.perform(patch("/api/v1/invoices/1/mark-paid")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentMethod": "CASH",
                                  "paidAmount": 110
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void legitimateInvoiceListingStillWorks() throws Exception {
        // Removing the legacy endpoint must not disturb the retained invoice
        // endpoints (create/read/list/cancel remain available).
        mockMvc.perform(get("/api/v1/invoices")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk());
    }
}