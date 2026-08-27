package com.erp.platform.security.sso;

/** The verified identity extracted from an OIDC ID token. */
public record OidcUser(String email, String name, boolean emailVerified, String subject) {}
