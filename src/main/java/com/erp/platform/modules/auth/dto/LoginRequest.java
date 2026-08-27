package com.erp.platform.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * The login identifier: an e-mail address or a username.
     *
     * <p>Kept under the name `email` because that is what the field has always been called on the
     * wire and renaming it would break every existing client. The @Email constraint that used to sit
     * here rejected a plain username with a validation error before the request ever reached the
     * service — which already resolved either form, as does the UserDetailsService behind it. So
     * username login was fully implemented and unreachable.
     */
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
