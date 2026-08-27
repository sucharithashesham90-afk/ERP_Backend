package com.erp.platform.modules.hr.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Who may see whose HR record.
 *
 * <p>One place decides this, because the rule is strict and the cost of getting it wrong in one
 * controller out of nineteen is somebody reading a colleague's salary. HR sees everyone. Everybody
 * else sees themselves and no one else — not their team, not their manager's, only their own.
 *
 * <p>Enforced here rather than in the browser. Every HR endpoint was
 * {@code isAuthenticated()}, so hiding a screen would have left the data one request away from any
 * signed-in user.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HrAccessService {

    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;
    private final UserContext userContext;

    /** Roles that may see every employee's data. Anything else is self-service only. */
    private static final Set<String> HR_ROLES = Set.of(
            "ROLE_ADMIN", "ROLE_TENANT_ADMIN", "ROLE_HR", "ROLE_HR_MANAGER", "ROLE_PAYROLL_MANAGER");

    /** True when the signed-in user may see everyone. */
    public boolean isHr() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        Set<String> held = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.toUpperCase())
                .collect(Collectors.toSet());
        return held.stream().anyMatch(HR_ROLES::contains);
    }

    /**
     * The employee record belonging to the signed-in user, if there is one.
     *
     * <p>Matched on the user id the employee record carries, falling back to the login email —
     * records created before employees were linked to logins have no user id, and their owner
     * should still reach their own payslip.
     */
    public Optional<Employee> currentEmployee() {
        UUID tenantId = tenantContext.current();
        UUID userId = userContext.getUserId();
        if (userId != null) {
            var byUser = employeeRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(tenantId, userId);
            if (byUser.isPresent()) return byUser;
        }
        String email = currentEmail();
        if (email != null && !email.isBlank()) {
            return employeeRepository.findFirstByTenantIdAndEmailIgnoreCaseAndDeletedAtIsNull(tenantId, email);
        }
        return Optional.empty();
    }

    /** The signed-in user's own employee id, or a refusal explaining why there is none. */
    public UUID requireCurrentEmployeeId() {
        return currentEmployee().map(Employee::getId).orElseThrow(() -> AppException.badRequest(
                "Your login is not linked to an employee record, so there is nothing of yours to show. "
                + "Ask HR to link your account to your employee profile."));
    }

    /**
     * The employee whose data a request may read.
     *
     * <p>HR gets whatever it asked for, including nothing, which means everyone. Anyone else gets
     * their own id regardless of what they asked for — passing somebody else's id is refused rather
     * than quietly redirected, so a caller probing for another record is told no.
     */
    public UUID scopeToViewable(UUID requestedEmployeeId) {
        if (isHr()) return requestedEmployeeId;
        UUID own = requireCurrentEmployeeId();
        if (requestedEmployeeId != null && !requestedEmployeeId.equals(own)) {
            log.warn("Employee {} attempted to read HR data for {}", own, requestedEmployeeId);
            throw AppException.forbidden("You can only view your own records.");
        }
        return own;
    }

    /**
     * The values a text {@code employee_id} column may hold for the signed-in user.
     *
     * <p>TA/DA claims and pay details store the employee as text rather than a foreign key, and what
     * went in depends on the screen: usually the employee's id as a string, sometimes their code.
     * Matching on both means an employee sees all of their own rows rather than an empty list.
     */
    public java.util.List<String> currentEmployeeKeys() {
        Employee self = currentEmployee().orElseThrow(() -> AppException.badRequest(
                "Your login is not linked to an employee record, so there is nothing of yours to show. "
                + "Ask HR to link your account to your employee profile."));
        java.util.List<String> keys = new java.util.ArrayList<>(2);
        keys.add(self.getId().toString());
        if (self.getEmployeeCode() != null && !self.getEmployeeCode().isBlank()) keys.add(self.getEmployeeCode());
        return keys;
    }

    /** Refuse unless the caller is HR or the record is the caller's own. */
    public void assertCanSee(UUID employeeIdOnRecord) {
        if (isHr()) return;
        UUID own = requireCurrentEmployeeId();
        if (employeeIdOnRecord == null || !employeeIdOnRecord.equals(own)) {
            throw AppException.forbidden("You can only view your own records.");
        }
    }

    /**
     * Refuse unless the caller is HR or the record is the caller's own, for records that store the
     * employee as text (an id string or an employee code) rather than a foreign key.
     */
    public void assertCanSeeKey(String employeeIdOnRecord) {
        if (isHr()) return;
        if (employeeIdOnRecord == null || !currentEmployeeKeys().contains(employeeIdOnRecord)) {
            throw AppException.forbidden("You can only view your own records.");
        }
    }

    /** Refuse unless the caller is HR. For anything that changes another person's record. */
    public void assertHr() {
        if (!isHr()) throw AppException.forbidden("Only HR can do that.");
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof org.springframework.security.core.userdetails.UserDetails ud) return ud.getUsername();
        return auth.getName();
    }
}
