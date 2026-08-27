package com.erp.platform.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private UUID tenantId;
    private String email;
    private String fullName;
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresIn;
    private List<String> roles;
    private List<String> permissions;
    private UUID groupId;
    private String groupName;
    private List<String> allowedLocations;
    private List<String> allowedModules;
    private List<String> allowedScreens;
    /** True for a login still on its temp password — the frontend routes straight to the forced
     *  change-password screen; the backend enforces it independently on every other request. */
    private boolean changePasswordOnLogin;
}
