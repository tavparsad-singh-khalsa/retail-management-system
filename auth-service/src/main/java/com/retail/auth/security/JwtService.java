package com.retail.auth.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor

public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey(){
        byte[] convertKey = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(convertKey);
    }

    public String generateToken(UserDetails userDetails){
        Date now = new Date();
        return Jwts.builder().subject(userDetails.getUsername())
                .claim("role", extractRoleName(userDetails))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .signWith(getSigningKey())
                .compact();

    }

    private String extractRoleName(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().startsWith("ROLE_")
                        ? authority.getAuthority().substring("ROLE_".length())
                        : authority.getAuthority())
                .orElse("");
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build().parseSignedClaims(token).getPayload();
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }
    public String extractUsername(String token){
        return extractClaim(token , Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);

    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(
            String token,
            UserDetails userDetails) {

        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);

    }

}
