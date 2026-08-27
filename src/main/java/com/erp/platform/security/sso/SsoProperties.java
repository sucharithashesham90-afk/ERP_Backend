package com.erp.platform.security.sso;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SSO / OIDC configuration. A provider is considered active only when its
 * {@code client-id} is set, so SSO stays off until deliberately configured.
 *
 * <pre>
 * app.sso.enabled=true
 * app.sso.providers.google.client-id=xxxx.apps.googleusercontent.com
 * app.sso.providers.google.issuer=https://accounts.google.com
 * app.sso.providers.google.jwks-uri=https://www.googleapis.com/oauth2/v3/certs
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "app.sso")
@Getter
@Setter
public class SsoProperties {

    /** Master switch. When false, the /auth/sso endpoint rejects all requests. */
    private boolean enabled = false;

    /** Create a local account on first SSO login if none exists for the email. */
    private boolean autoProvision = true;

    /** Reject tokens whose email the provider has not verified. */
    private boolean requireEmailVerified = true;

    /** Role assigned to auto-provisioned users. */
    private String defaultRole = "STAFF";

    /** Tenant assigned to auto-provisioned users. */
    private String defaultTenantId = "00000000-0000-0000-0000-000000000001";

    /** OIDC providers keyed by name (google, microsoft, okta, …). */
    private Map<String, Provider> providers = new LinkedHashMap<>();

    public Provider provider(String name) {
        return name == null ? null : providers.get(name.toLowerCase());
    }

    @Getter
    @Setter
    public static class Provider {
        /** OAuth client id — also the expected token audience. Blank = provider disabled. */
        private String clientId;
        /** Expected token issuer (iss claim). */
        private String issuer;
        /** URL of the provider's JSON Web Key Set. */
        private String jwksUri;

        public boolean isActive() {
            return clientId != null && !clientId.isBlank();
        }
    }
}
