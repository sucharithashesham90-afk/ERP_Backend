package com.erp.platform.modules.auth.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.auth.dto.AuthResponse;
import com.erp.platform.modules.auth.dto.LoginRequest;
import com.erp.platform.modules.auth.dto.RegisterRequest;
import com.erp.platform.modules.auth.dto.UpdateUserRequest;
import com.erp.platform.modules.auth.dto.UserDto;
import com.erp.platform.modules.auth.service.AuthService;
import com.erp.platform.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, registration, and user management")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, ip)));
    }

    private String resolveClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return req.getRemoteAddr();
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Register a new user (administrators only — the caller controls the assigned roles)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request), "User registered successfully"));
    }

    @GetMapping("/sso/config")
    @Operation(summary = "Which SSO providers are available to the login screen")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ssoConfig() {
        return ResponseEntity.ok(ApiResponse.success(authService.ssoConfig()));
    }

    @PostMapping("/sso")
    @Operation(summary = "Sign in with an OIDC ID token (Google / Microsoft / …)")
    public ResponseEntity<ApiResponse<AuthResponse>> sso(@RequestBody Map<String, Object> body) {
        String provider = body.get("provider") != null ? body.get("provider").toString() : null;
        String idToken = body.get("idToken") != null ? body.get("idToken").toString() : null;
        return ResponseEntity.ok(ApiResponse.success(authService.ssoLogin(provider, idToken), "Signed in"));
    }

    // ---- Self service ----
    //
    // A user maintaining their own details is a different operation from an administrator
    // maintaining someone else's, and must not share an endpoint: the admin request body carries
    // roles, privilege level and expiry dates, which nobody should be able to set on themselves.

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "The signed-in user's own profile")
    public ResponseEntity<ApiResponse<UserDto>> me() {
        return ResponseEntity.ok(ApiResponse.success(authService.me()));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update your own profile — contact details only")
    public ResponseEntity<ApiResponse<UserDto>> updateMe(@RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateOwnProfile(request), "Profile updated"));
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change your own password")
    public ResponseEntity<ApiResponse<Void>> changeOwnPassword(@RequestBody Map<String, String> body) {
        authService.changeOwnPassword(body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed"));
    }

    // ---- User Management (administrators only) ----

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "List all users for the current tenant")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(authService.listUsers(pageable)));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateUser(id, request)));
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Update user roles")
    public ResponseEntity<ApiResponse<UserDto>> updateRoles(
            @PathVariable UUID id,
            @RequestBody Map<String, List<String>> body) {
        List<String> roles = body.get("roles");
        return ResponseEntity.ok(ApiResponse.success(authService.updateRoles(id, roles)));
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Activate or deactivate a user")
    public ResponseEntity<ApiResponse<UserDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(ApiResponse.success(authService.updateStatus(id, active)));
    }

    @PutMapping("/users/{id}/locations")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Assign allowed locations to a user (empty list = access all)")
    public ResponseEntity<ApiResponse<UserDto>> updateLocations(
            @PathVariable UUID id,
            @RequestBody Map<String, List<String>> body) {
        return ResponseEntity.ok(ApiResponse.success(authService.updateLocations(id, body.get("locations"))));
    }

    @GetMapping("/users/expiring")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "List users whose accounts expire within next 30 days")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listExpiring(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "30") int daysAhead) {
        var pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return ResponseEntity.ok(ApiResponse.success(authService.listExpiringUsers(pageable, daysAhead)));
    }

    @GetMapping("/users/expired")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "List users whose accounts have expired")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listExpired(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("expiryDate").descending());
        return ResponseEntity.ok(ApiResponse.success(authService.listExpiredUsers(pageable)));
    }

    @PatchMapping("/users/{id}/expiry")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Update user account expiry date")
    public ResponseEntity<ApiResponse<UserDto>> updateExpiry(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        java.time.LocalDate newDate = body.get("expiryDate") != null
                ? java.time.LocalDate.parse(body.get("expiryDate"))
                : null;
        return ResponseEntity.ok(ApiResponse.success(authService.updateExpiry(id, newDate)));
    }

    @PutMapping("/users/{id}/group")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Assign or remove a user's group (null groupId removes group)")
    public ResponseEntity<ApiResponse<UserDto>> updateGroup(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        UUID groupId = body.get("groupId") != null
                ? UUID.fromString(body.get("groupId").toString())
                : null;
        return ResponseEntity.ok(ApiResponse.success(authService.updateGroup(id, groupId)));
    }

    @PutMapping("/users/{id}/groups")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Assign a user's group memberships (multi-select) and reporting manager — only for users with no linked Employee")
    public ResponseEntity<ApiResponse<UserDto>> updateGroups(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> raw = (java.util.List<String>) body.getOrDefault("groupIds", java.util.List.of());
        java.util.Set<UUID> groupIds = raw.stream().map(UUID::fromString).collect(java.util.stream.Collectors.toSet());
        UUID reportingManagerId = body.get("reportingManagerId") != null
                ? UUID.fromString(body.get("reportingManagerId").toString())
                : null;
        return ResponseEntity.ok(ApiResponse.success(authService.updateGroups(id, groupIds, reportingManagerId)));
    }

    // ---- User Approval Workflow ----

    @GetMapping("/users/pending-approvals")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "List all users pending approval for this tenant")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(authService.listPendingApprovals(pageable)));
    }

    @GetMapping("/users/pending-approvals/my-queue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List pending users whose reporting manager is the current user")
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> myPendingApprovals(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        String token = authHeader.replace("Bearer ", "");
        UUID managerId = tokenProvider.getUserIdFromToken(token);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return ResponseEntity.ok(ApiResponse.success(authService.listPendingApprovalsForManager(managerId, pageable)));
    }

    @PostMapping("/users/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve a pending user — sets status to ACTIVE")
    public ResponseEntity<ApiResponse<UserDto>> approveUser(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        UUID approvedById = tokenProvider.getUserIdFromToken(token);
        String remarks = body != null ? body.get("remarks") : null;
        return ResponseEntity.ok(ApiResponse.success(authService.approveUser(id, approvedById, remarks), "User approved"));
    }

    @PostMapping("/users/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject a pending user — sets status to REJECTED")
    public ResponseEntity<ApiResponse<UserDto>> rejectUser(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        UUID rejectedById = tokenProvider.getUserIdFromToken(token);
        String remarks = body != null ? body.get("remarks") : null;
        return ResponseEntity.ok(ApiResponse.success(authService.rejectUser(id, rejectedById, remarks), "User rejected"));
    }
}
