package com.erp.platform.modules.auth.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.admin.entity.LoginTracking;
import com.erp.platform.modules.admin.repository.LoginTrackingRepository;
import com.erp.platform.modules.auth.dto.AuthResponse;
import com.erp.platform.modules.auth.dto.LoginRequest;
import com.erp.platform.modules.auth.dto.RegisterRequest;
import com.erp.platform.modules.auth.dto.UpdateUserRequest;
import com.erp.platform.modules.auth.dto.UserDto;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.entity.UserGroup;
import com.erp.platform.modules.auth.repository.RoleRepository;
import com.erp.platform.modules.auth.repository.UserGroupRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.security.JwtTokenProvider;
import com.erp.platform.security.sso.OidcTokenVerifier;
import com.erp.platform.security.sso.OidcUser;
import com.erp.platform.security.sso.SsoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.inventory.repository.LocationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserGroupRepository userGroupRepository;
    private final LocationRepository locationRepository;
    private final EmployeeRepository employeeRepository;
    private final LoginTrackingRepository loginTrackingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final TenantContext tenantContext;
    private final SsoProperties ssoProperties;
    private final OidcTokenVerifier oidcVerifier;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email already registered: " + request.getEmail());
        }

        // Resolve role: prefer roles list, then roleName, then default STAFF
        final String resolvedRoleName = (request.getRoles() != null && !request.getRoles().isEmpty())
                ? request.getRoles().get(0)
                : (StringUtils.hasText(request.getRoleName()) ? request.getRoleName() : "STAFF");

        Role role = roleRepository.findByName(resolvedRoleName)
                .orElseThrow(() -> AppException.notFound("Role not found: " + resolvedRoleName));

        // Resolve tenantId: use from request or from current context if available
        UUID tenantId = request.getTenantId();
        if (tenantId == null) {
            tenantId = tenantContext.isSet() ? tenantContext.current() : UUID.randomUUID();
        }

        // Resolve full name: prefer firstName+lastName, then fullName, then email prefix
        String resolvedFullName;
        if (StringUtils.hasText(request.getFirstName())) {
            resolvedFullName = request.getFirstName()
                    + (StringUtils.hasText(request.getLastName()) ? " " + request.getLastName() : "");
        } else if (StringUtils.hasText(request.getFullName())) {
            resolvedFullName = request.getFullName();
        } else {
            resolvedFullName = request.getEmail().split("@")[0];
        }

        User user = new User();
        user.setFullName(resolvedFullName);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());
        user.setTenantId(tenantId);
        user.setRoles(new java.util.HashSet<>(java.util.Set.of(role)));
        user.setTitle(request.getTitle());
        user.setMobile(request.getMobile());
        user.setFax(request.getFax());
        user.setOfficeAddress(request.getOfficeAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setZip(request.getZip());
        user.setSalesRepId(request.getSalesRepId());
        user.setPrivilegeLevel(request.getPrivilegeLevel());
        user.setChangePasswordOnLogin(request.isChangePasswordOnLogin());
        user.setActivationDate(request.getActivationDate());
        user.setExpiryDate(request.getExpiryDate());
        user.setStatus(User.UserStatus.PENDING_APPROVAL);

        if (request.getReportingManagerId() != null) {
            user.setReportingManagerId(request.getReportingManagerId());
            final User userRef = user;
            userRepository.findById(request.getReportingManagerId())
                    .ifPresent(m -> userRef.setReportingManagerName(m.getFullName()));
        }

        if (request.getGroupId() != null) {
            final UUID effectiveTenantId = tenantId;
            final User newUser = user;
            userGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(effectiveTenantId, request.getGroupId())
                    .ifPresent(g -> { newUser.setGroupId(g.getId()); newUser.setGroupName(g.getName()); });
        }

        user = userRepository.save(user);

        log.info("Registered new user: email={}", user.getEmail());
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        // request.getEmail() is the login identifier — resolve by e-mail or username.
        User user = userRepository.findByEmail(request.getEmail())
                .or(() -> userRepository.findByUsername(request.getEmail()))
                .orElseThrow(() -> AppException.notFound("User not found"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException | LockedException ex) {
            // UserDetailsServiceImpl blocks PENDING_APPROVAL/REJECTED/INACTIVE/LOCKED at the
            // Spring Security layer before we'd ever see them here — translate using the user's
            // actual status instead of letting a generic "Authentication required" swallow the reason.
            throw blockedStatusException(user);
        }

        if (user.isChangePasswordOnLogin() && user.getPasswordExpiresAt() != null
                && LocalDateTime.now().isAfter(user.getPasswordExpiresAt())) {
            throw AppException.passwordExpired(
                    "Your temporary password has expired. Contact HR or your manager to reset it.");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        LoginTracking tracking = new LoginTracking();
        tracking.setTenantId(user.getTenantId());
        tracking.setUserId(user.getId().toString());
        tracking.setUsername(user.getEmail());
        tracking.setLoginTime(now);
        tracking.setIpAddress(ipAddress);
        tracking.setStatus("ACTIVE");
        loginTrackingRepository.save(tracking);

        log.info("Login successful: email={}", user.getEmail());
        return buildResponse(user);
    }

    private AppException blockedStatusException(User user) {
        return switch (user.getStatus()) {
            case PENDING_APPROVAL -> AppException.accountPendingApproval(
                    "Your account is awaiting approval from your manager.");
            case REJECTED -> AppException.accountRejected("Your account was rejected"
                    + (StringUtils.hasText(user.getApprovalRemarks()) ? ": " + user.getApprovalRemarks() : "."));
            case LOCKED -> AppException.forbidden("This account is locked.");
            default -> AppException.forbidden("This account is inactive.");
        };
    }

    // ---- Single Sign-On (OIDC) ----

    /** Which SSO providers the frontend should render (only those with a configured client id). */
    public Map<String, Object> ssoConfig() {
        List<Map<String, Object>> provs = new ArrayList<>();
        ssoProperties.getProviders().forEach((name, p) -> {
            if (p.isActive()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", name);
                m.put("clientId", p.getClientId());
                m.put("issuer", p.getIssuer());   // lets the frontend derive the Microsoft/MSAL authority
                provs.add(m);
            }
        });
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", ssoProperties.isEnabled() && !provs.isEmpty());
        out.put("providers", provs);
        return out;
    }

    /** Exchange a verified OIDC ID token for an application session (JWT). */
    @Transactional
    public AuthResponse ssoLogin(String providerName, String idToken) {
        if (!ssoProperties.isEnabled()) throw AppException.forbidden("Single sign-on is not enabled");
        SsoProperties.Provider provider = ssoProperties.provider(providerName);
        if (provider == null || !provider.isActive())
            throw AppException.badRequest("Unknown or unconfigured SSO provider: " + providerName);

        OidcUser oidc = oidcVerifier.verify(provider, idToken);
        if (ssoProperties.isRequireEmailVerified() && !oidc.emailVerified())
            throw AppException.forbidden("Your email is not verified with the identity provider");
        String email = oidc.email();
        if (email == null || email.isBlank())
            throw AppException.badRequest("The SSO token did not contain an email address");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            if (!ssoProperties.isAutoProvision())
                throw AppException.forbidden("No account exists for " + email + ". Please contact your administrator.");
            user = provisionSsoUser(email, oidc.name());
            log.info("SSO auto-provisioned new user: email={}", email);
        }
        if (user.getStatus() != User.UserStatus.ACTIVE)
            throw AppException.forbidden("This account is not active");

        user.setEmailVerified(true);
        user.setLastLoginAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("SSO login successful: provider={}, email={}", providerName, email);
        return buildResponse(user);
    }

    private User provisionSsoUser(String email, String name) {
        Role role = roleRepository.findByName(ssoProperties.getDefaultRole())
                .orElseThrow(() -> AppException.notFound("Default SSO role not found: " + ssoProperties.getDefaultRole()));
        User u = new User();
        u.setEmail(email);
        u.setFullName(name != null && !name.isBlank() ? name : email.split("@")[0]);
        u.setUsername(email.split("@")[0]);
        u.setPassword(passwordEncoder.encode(UUID.randomUUID() + ":" + UUID.randomUUID()));
        u.setTenantId(UUID.fromString(ssoProperties.getDefaultTenantId()));
        u.setRoles(new java.util.HashSet<>(java.util.Set.of(role)));
        u.setStatus(User.UserStatus.ACTIVE);
        u.setEmailVerified(true);
        return userRepository.save(u);
    }

    // ---- User Management ----

    public PageResponse<UserDto> listUsers(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(userRepository.findByTenantId(tenantId, pageable).map(this::toUserDto));
    }

    /** The signed-in user, resolved from the security context (the principal is their email). */
    public User currentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw AppException.forbidden("Not signed in");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> AppException.notFound("Signed-in user not found: " + auth.getName()));
    }

    public UserDto me() {
        return toUserDto(currentUser());
    }

    /**
     * A user editing their own details.
     *
     * <p>Deliberately narrower than {@link #updateUser}: it touches contact and personal fields
     * only. Roles, privilege level, activation and expiry dates, and the username decide what
     * someone is allowed to do and for how long — letting a user set those on themselves is
     * privilege escalation, so those arrive here and are ignored.
     */
    @Transactional
    public UserDto updateOwnProfile(UpdateUserRequest request) {
        User user = currentUser();

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFullName(request.getFirstName()
                    + (StringUtils.hasText(request.getLastName()) ? " " + request.getLastName() : ""));
        } else if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw AppException.conflict("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getTitle() != null)         user.setTitle(request.getTitle());
        if (request.getPhone() != null)         user.setPhone(request.getPhone());
        if (request.getMobile() != null)        user.setMobile(request.getMobile());
        if (request.getFax() != null)           user.setFax(request.getFax());
        if (request.getOfficeAddress() != null) user.setOfficeAddress(request.getOfficeAddress());
        if (request.getCity() != null)          user.setCity(request.getCity());
        if (request.getState() != null)         user.setState(request.getState());
        if (request.getZip() != null)           user.setZip(request.getZip());

        log.info("User {} updated their own profile", user.getEmail());
        return toUserDto(userRepository.save(user));
    }

    /** Change one's own password, which requires proving the current one. */
    @Transactional
    public void changeOwnPassword(String currentPassword, String newPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw AppException.badRequest("New password must be at least 8 characters");
        }
        User user = currentUser();
        if (!passwordEncoder.matches(currentPassword == null ? "" : currentPassword, user.getPassword())) {
            throw AppException.badRequest("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setChangePasswordOnLogin(false);
        userRepository.save(user);
        log.info("User {} changed their own password", user.getEmail());
    }

    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));

        if (StringUtils.hasText(request.getFirstName())) {
            String fullName = request.getFirstName()
                    + (StringUtils.hasText(request.getLastName()) ? " " + request.getLastName() : "");
            user.setFullName(fullName);
        } else if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw AppException.conflict("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (request.getTitle() != null) user.setTitle(request.getTitle());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getFax() != null) user.setFax(request.getFax());
        if (request.getOfficeAddress() != null) user.setOfficeAddress(request.getOfficeAddress());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getZip() != null) user.setZip(request.getZip());
        if (request.getSalesRepId() != null) user.setSalesRepId(request.getSalesRepId());
        if (request.getPrivilegeLevel() != null) user.setPrivilegeLevel(request.getPrivilegeLevel());
        if (request.getChangePasswordOnLogin() != null) user.setChangePasswordOnLogin(request.getChangePasswordOnLogin());
        if (request.getActivationDate() != null) user.setActivationDate(request.getActivationDate());
        if (request.getExpiryDate() != null) user.setExpiryDate(request.getExpiryDate());
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateRoles(UUID id, List<String> roleNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> AppException.notFound("Role not found: " + name)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateGroup(UUID id, UUID groupId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        if (groupId == null) {
            user.setGroupId(null);
            user.setGroupName(null);
        } else {
            UserGroup group = userGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(user.getTenantId(), groupId)
                    .orElseThrow(() -> AppException.notFound("Group not found: " + groupId));
            user.setGroupId(group.getId());
            user.setGroupName(group.getName());
        }
        return toUserDto(userRepository.save(user));
    }

    /**
     * Multi-group membership + reporting manager. Employee is the source of truth once a User is
     * linked to one (see EmployeeService.syncGroupsAndManagerToUser) — editing these directly here
     * is only for standalone accounts with no linked Employee (e.g. the initial admin login), so a
     * save would otherwise be silently overwritten the next time the linked Employee is saved.
     */
    @Transactional
    public UserDto updateGroups(UUID id, Set<UUID> groupIds, UUID reportingManagerId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        boolean linkedToEmployee = employeeRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(user.getTenantId(), user.getId()).isPresent();
        if (linkedToEmployee) {
            throw AppException.badRequest("This user's groups and reporting manager are managed from their Employee record — edit them there instead.");
        }
        Set<UUID> ids = groupIds != null ? groupIds : Set.of();
        user.setGroupIds(new HashSet<>(ids));
        if (!ids.isEmpty()) {
            UUID primary = ids.iterator().next();
            userGroupRepository.findByTenantIdAndIdAndDeletedAtIsNull(user.getTenantId(), primary)
                    .ifPresentOrElse(g -> { user.setGroupId(g.getId()); user.setGroupName(g.getName()); },
                            () -> { user.setGroupId(null); user.setGroupName(null); });
        } else {
            user.setGroupId(null);
            user.setGroupName(null);
        }
        if (reportingManagerId == null) {
            user.setReportingManagerId(null);
            user.setReportingManagerName(null);
        } else {
            User manager = userRepository.findById(reportingManagerId)
                    .orElseThrow(() -> AppException.notFound("Reporting manager not found: " + reportingManagerId));
            user.setReportingManagerId(manager.getId());
            user.setReportingManagerName(manager.getFullName());
        }
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto approveUser(UUID id, UUID approvedById, String remarks) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        if (user.getStatus() != User.UserStatus.PENDING_APPROVAL) {
            throw AppException.conflict("User is not in PENDING_APPROVAL state");
        }
        requireApprover(user, approvedById);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setApprovedBy(approvedById);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovalRemarks(remarks);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto rejectUser(UUID id, UUID rejectedById, String remarks) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        if (user.getStatus() != User.UserStatus.PENDING_APPROVAL) {
            throw AppException.conflict("User is not in PENDING_APPROVAL state");
        }
        requireApprover(user, rejectedById);
        user.setStatus(User.UserStatus.REJECTED);
        user.setApprovedBy(rejectedById);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovalRemarks(remarks);
        return toUserDto(userRepository.save(user));
    }

    public PageResponse<UserDto> listPendingApprovals(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(userRepository.findPendingApprovals(tenantId, pageable).map(this::toUserDto));
    }

    public PageResponse<UserDto> listPendingApprovalsForManager(UUID managerId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(userRepository.findPendingApprovalsForManager(tenantId, managerId, pageable).map(this::toUserDto));
    }

    @Transactional
    public UserDto updateStatus(UUID id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        user.setStatus(active ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateLocations(UUID id, List<String> locationNames) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        user.setAllowedLocations(locationNames == null ? new HashSet<>() : new HashSet<>(locationNames));
        return toUserDto(userRepository.save(user));
    }

    public PageResponse<UserDto> listExpiringUsers(Pageable pageable, int daysAhead) {
        UUID tenantId = tenantContext.current();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate cutoff = today.plusDays(daysAhead);
        return PageResponse.of(userRepository.findExpiringUsers(tenantId, today, cutoff, pageable).map(this::toUserDto));
    }

    public PageResponse<UserDto> listExpiredUsers(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        java.time.LocalDate today = java.time.LocalDate.now();
        return PageResponse.of(userRepository.findExpiredUsers(tenantId, today, pageable).map(this::toUserDto));
    }

    @Transactional
    public UserDto updateExpiry(UUID id, java.time.LocalDate newDate) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User not found: " + id));
        user.setExpiryDate(newDate);
        return toUserDto(userRepository.save(user));
    }

    /**
     * Only the account's specific reporting manager may approve/reject it — no bypass — for
     * accounts that have one (e.g. provisioned when HR created the linked Employee). Accounts with
     * no reporting manager on record — a top-level position with no peer manager, or a standalone
     * registration — fall back to admin-only, rather than the previous "any authenticated caller."
     */
    private void requireApprover(User target, UUID callerId) {
        if (target.getReportingManagerId() != null) {
            if (!target.getReportingManagerId().equals(callerId)) {
                throw AppException.forbidden("Only this user's reporting manager can approve or reject their account.");
            }
            return;
        }
        // No reporting manager — a top-level position, with no peer who could ever approve it.
        // Only an admin may act, not just anyone signed in.
        boolean callerIsAdmin = userRepository.findById(callerId)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()) || "TENANT_ADMIN".equals(r.getName())))
                .orElse(false);
        if (!callerIsAdmin) {
            throw AppException.forbidden("This account has no reporting manager — only an admin can approve or reject it.");
        }
    }

    // ---- Private helpers ----

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername() != null ? user.getUsername()
                : user.getEmail().split("@")[0]);
        dto.setFullName(user.getFullName());
        String fullName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        String[] parts = fullName.split(" ", 2);
        dto.setFirstName(parts[0]);
        dto.setLastName(parts.length > 1 ? parts[1] : "");
        dto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        dto.setGroupId(user.getGroupId());
        dto.setGroupName(user.getGroupName());
        dto.setGroupIds(user.getGroupIds());
        dto.setLinkedToEmployee(employeeRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(user.getTenantId(), user.getId()).isPresent());
        dto.setAllowedLocations(new ArrayList<>(user.getAllowedLocations()));
        dto.setActive(user.isActive());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setTitle(user.getTitle());
        dto.setPhone(user.getPhone());
        dto.setMobile(user.getMobile());
        dto.setFax(user.getFax());
        dto.setOfficeAddress(user.getOfficeAddress());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setZip(user.getZip());
        dto.setSalesRepId(user.getSalesRepId());
        dto.setPrivilegeLevel(user.getPrivilegeLevel());
        dto.setChangePasswordOnLogin(user.isChangePasswordOnLogin());
        dto.setPasswordExpiresAt(user.getPasswordExpiresAt());
        dto.setActivationDate(user.getActivationDate());
        dto.setExpiryDate(user.getExpiryDate());
        dto.setReportingManagerId(user.getReportingManagerId());
        dto.setReportingManagerName(user.getReportingManagerName());
        dto.setApprovalRemarks(user.getApprovalRemarks());
        dto.setApprovedBy(user.getApprovedBy());
        dto.setApprovedAt(user.getApprovedAt());
        return dto;
    }

    private AuthResponse buildResponse(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .distinct()
                .collect(Collectors.toList());

        List<String> locations = new ArrayList<>(user.getAllowedLocations());
        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getTenantId(), roles, locations);

        // Collect module/screen access from custom (non-system) roles
        List<String> allowedModules = user.getRoles().stream()
                .filter(r -> !r.isSystem() && r.getAllowedModules() != null && !r.getAllowedModules().isEmpty())
                .flatMap(r -> r.getAllowedModules().stream())
                .distinct()
                .collect(Collectors.toList());

        List<String> allowedScreens = user.getRoles().stream()
                .filter(r -> !r.isSystem() && r.getAllowedScreens() != null && !r.getAllowedScreens().isEmpty())
                .flatMap(r -> r.getAllowedScreens().stream())
                .distinct()
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .token(token)
                .expiresIn(jwtExpirationMs / 1000)
                .roles(roles)
                .permissions(permissions)
                .groupId(user.getGroupId())
                .groupName(user.getGroupName())
                .allowedLocations(locations)
                .allowedModules(allowedModules.isEmpty() ? null : allowedModules)
                .allowedScreens(allowedScreens.isEmpty() ? null : allowedScreens)
                .changePasswordOnLogin(user.isChangePasswordOnLogin())
                .build();
    }
}
