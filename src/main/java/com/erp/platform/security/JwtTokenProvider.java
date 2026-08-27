package com.erp.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, UUID userId, UUID tenantId, List<String> roles) {
        return generateToken(email, userId, tenantId, roles, java.util.Collections.emptyList());
    }

    public String generateToken(String email, UUID userId, UUID tenantId, List<String> roles, List<String> locations) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("roles", roles)
                .claim("locations", locations)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID getTenantIdFromToken(String token) {
        String tenantId = (String) parseClaims(token).get("tenantId");
        return UUID.fromString(tenantId);
    }

    public UUID getUserIdFromToken(String token) {
        String userId = (String) parseClaims(token).get("userId");
        return UUID.fromString(userId);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    @SuppressWarnings("unchecked")
    public List<String> getLocationsFromToken(String token) {
        Object val = parseClaims(token).get("locations");
        if (val instanceof List<?>) return (List<String>) val;
        return java.util.Collections.emptyList();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported");
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed");
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
