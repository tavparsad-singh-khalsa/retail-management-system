package com.retail.billingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-for-billing-role-claim-0123456789";

    @Test
    void extractRoleReturnsRoleClaimWhenPresent() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("owner")
                .claim("role", "OWNER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        JwtService jwtService = service();

        assertEquals("OWNER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("owner", jwtService.extractUsername(token));
    }

    @Test
    void extractRoleReturnsNullWhenNoRoleClaim() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("owner")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        JwtService jwtService = service();

        assertNull(jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractsClaimsFromAuthServiceSignedToken() {
        JwtService jwtService = service();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("owner")
                .claim("role", "OWNER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("owner", claims.getSubject());
        assertEquals("OWNER", claims.get("role", String.class));
    }

    private JwtService service() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        return jwtService;
    }
}