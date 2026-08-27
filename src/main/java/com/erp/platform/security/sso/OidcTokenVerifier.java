package com.erp.platform.security.sso;

import com.erp.platform.common.exception.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verifies an OIDC ID token the way a resource server should: the RS256 signature
 * is checked against the provider's published JWKS (keys cached, refreshed on rotation),
 * then the issuer, audience and expiry claims are validated. No token is trusted on decode alone.
 */
@Component
@Slf4j
public class OidcTokenVerifier {

    private static final long JWKS_TTL_MS = 3_600_000L; // 1h

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final Map<String, CachedKeys> cache = new ConcurrentHashMap<>();

    public OidcUser verify(SsoProperties.Provider provider, String idToken) {
        if (idToken == null || idToken.isBlank()) throw AppException.badRequest("Missing ID token");

        final Jws<Claims> jws;
        try {
            jws = Jwts.parser()
                    .keyLocator(new Locator<Key>() {
                        @Override public Key locate(Header header) {
                            Object kid = header.get("kid");
                            return resolveKey(provider.getJwksUri(), kid == null ? null : kid.toString());
                        }
                    })
                    .clockSkewSeconds(120)
                    .build()
                    .parseSignedClaims(idToken);
        } catch (JwtException e) {
            throw AppException.forbidden("Invalid SSO token: " + e.getMessage());
        }

        Claims c = jws.getPayload();
        if (!issuerMatches(provider.getIssuer(), c.getIssuer()))
            throw AppException.forbidden("SSO token issuer not trusted");
        Set<String> aud = c.getAudience();
        if (aud == null || !aud.contains(provider.getClientId()))
            throw AppException.forbidden("SSO token was not issued for this application");

        // Google puts the address in "email"; Microsoft work/school accounts often omit it
        // and carry it in "preferred_username" (the UPN) instead.
        String email = c.get("email", String.class);
        if (email == null || email.isBlank())
            email = firstNonBlank(c.get("preferred_username", String.class), c.get("upn", String.class));
        String name = c.get("name", String.class);

        // Microsoft doesn't emit "email_verified" — the token comes from the organisation's own
        // tenant, so a validly-signed Microsoft token is inherently trusted.
        Boolean verifiedClaim = c.get("email_verified", Boolean.class);
        boolean emailVerified = verifiedClaim != null ? verifiedClaim : isMicrosoftIssuer(c.getIssuer());
        return new OidcUser(email, name, emailVerified, c.getSubject());
    }

    /** Accept the configured issuer, scheme-insensitively (Google issues both with and without https://). */
    private static boolean issuerMatches(String expected, String actual) {
        if (expected == null || actual == null) return false;
        return strip(expected).equals(strip(actual));
    }
    private static String strip(String s) { return s.replaceFirst("^https?://", "").replaceAll("/$", ""); }

    private static boolean isMicrosoftIssuer(String iss) {
        if (iss == null) return false;
        String h = iss.toLowerCase();
        return h.contains("login.microsoftonline.com") || h.contains("sts.windows.net");
    }
    private static String firstNonBlank(String... vals) {
        if (vals != null) for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private RSAPublicKey resolveKey(String jwksUri, String kid) {
        if (jwksUri == null || jwksUri.isBlank()) throw AppException.forbidden("SSO provider has no JWKS URI");
        if (kid == null) throw AppException.forbidden("SSO token has no key id");
        CachedKeys cached = cache.get(jwksUri);
        if (cached == null || cached.expiresAt < System.currentTimeMillis() || !cached.keys.containsKey(kid)) {
            cached = fetch(jwksUri);
            cache.put(jwksUri, cached);
        }
        RSAPublicKey key = cached.keys.get(kid);
        if (key == null) throw AppException.forbidden("No matching signing key for the SSO token");
        return key;
    }

    private CachedKeys fetch(String jwksUri) {
        try {
            HttpResponse<String> resp = httpClient.send(
                    HttpRequest.newBuilder(URI.create(jwksUri)).timeout(Duration.ofSeconds(8)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            JsonNode keys = mapper.readTree(resp.body()).path("keys");
            Map<String, RSAPublicKey> map = new HashMap<>();
            for (JsonNode k : keys) {
                if (!"RSA".equals(k.path("kty").asText())) continue;
                map.put(k.path("kid").asText(), toRsa(k.path("n").asText(), k.path("e").asText()));
            }
            return new CachedKeys(map, System.currentTimeMillis() + JWKS_TTL_MS);
        } catch (Exception e) {
            log.warn("JWKS fetch failed for {}: {}", jwksUri, e.getMessage());
            throw AppException.forbidden("Could not reach the identity provider to verify the token");
        }
    }

    private static RSAPublicKey toRsa(String n, String e) throws Exception {
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    private record CachedKeys(Map<String, RSAPublicKey> keys, long expiresAt) {}
}
