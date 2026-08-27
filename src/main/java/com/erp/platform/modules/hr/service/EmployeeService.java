package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.entity.UserGroup;
import com.erp.platform.modules.auth.repository.RoleRepository;
import com.erp.platform.modules.auth.repository.UserGroupRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.modules.hr.dto.CreateEmployeeRequest;
import com.erp.platform.modules.hr.dto.EmployeeDto;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeService {

    /** How long the default password HR sets stays valid before the account is locked out. */
    private static final int TEMP_PASSWORD_VALID_DAYS = 15;

    private final EmployeeRepository employeeRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;
    private final UserContext userContext;
    private final com.erp.platform.modules.accounting.service.PartyLedgerService partyLedgerService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;

    private String employeeName(Employee e) {
        return (nz(e.getFirstName()) + " " + nz(e.getLastName())).trim();
    }
    private static String nz(String s) { return s == null ? "" : s; }

    public PageResponse<EmployeeDto> list(String search, Pageable pageable) {
        return list(search, null, pageable);
    }

    /**
     * The employee directory: everyone for HR, yourself alone for anybody else.
     *
     * <p>An employee record carries salary, bank details and address, so this is the strictest of
     * the HR endpoints. A non-HR caller gets a single-row page of their own record whatever they
     * search for — the search never reaches the repository, so it cannot be used to probe for
     * colleagues by name.
     */
    public PageResponse<EmployeeDto> list(String search, UUID departmentId, Pageable pageable) {
        UUID tenantId = tenantContext.current();

        if (!hrAccess.isHr()) {
            var self = hrAccess.currentEmployee();
            List<EmployeeDto> one = self.map(e -> List.of(toDto(e))).orElseGet(List::of);
            return PageResponse.of(new org.springframework.data.domain.PageImpl<>(one, pageable, one.size()));
        }

        var page = departmentId != null
                ? employeeRepository.findByTenantIdAndDepartmentIdAndDeletedAtIsNull(tenantId, departmentId, pageable)
                : StringUtils.hasText(search)
                    ? employeeRepository.searchByTenantId(tenantId, search, pageable)
                    : employeeRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    /** One employee — refused unless it is the caller's own record, or the caller is HR. */
    public EmployeeDto getById(UUID id) {
        hrAccess.assertCanSee(id);
        return toDto(findOrThrow(id));
    }

    /** The Employee profile linked to the current user, or null if none is linked (e.g. a pure admin login). */
    public EmployeeDto getCurrentEmployee() {
        if (userContext.getUserId() == null) return null;
        return employeeRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(tenantContext.current(), userContext.getUserId())
                .map(this::toDto).orElse(null);
    }

    @Transactional
    public EmployeeDto create(CreateEmployeeRequest request) {
        UUID tenantId = tenantContext.current();
        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        applyRequest(employee, request);
        if (!StringUtils.hasText(employee.getEmployeeCode())) {
            employee.setEmployeeCode("EMP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")) + "-"
                    + String.format("%04d", employeeRepository.count() + 1));
        }
        if (employee.getStatus() == null) employee.setStatus(EmployeeStatus.ACTIVE);
        employee = employeeRepository.save(employee);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.EMPLOYEE, employeeName(employee), employee.getId(), employee.getEmployeeCode());

        boolean passwordGiven = StringUtils.hasText(request.getDefaultPassword());
        boolean roleGiven     = StringUtils.hasText(request.getRoleName());
        if (passwordGiven || roleGiven) {
            if (!passwordGiven || !roleGiven) {
                throw AppException.badRequest(
                        "Default Password and Role are both required together to create this employee's login.");
            }
            // Manager is independently optional — omitted for a top-level position (e.g. the first
            // hire), in which case an admin approves the account instead of a peer manager; see
            // AuthService.requireApprover().
            User user = provisionLogin(employee, request.getManagerId(), request.getDefaultPassword(), request.getRoleName());
            employee.setUserId(user.getId());
            employee = employeeRepository.save(employee);
            syncGroupsAndManagerToUser(employee);
        }

        log.info("Employee created: id={}, code={}", employee.getId(), employee.getEmployeeCode());
        return toDto(employee);
    }

    /**
     * Provisions the login HR grants this employee at creation time, using the default password
     * HR set — valid {@link #TEMP_PASSWORD_VALID_DAYS} days, gated on approval by the manager
     * Employee record the request points at. Mirrors the same shape built for erp-generic:
     * PENDING approval + a forced-change temp password, both enforced backend-side.
     */
    private User provisionLogin(Employee employee, UUID managerId, String defaultPassword, String roleName) {
        if (!StringUtils.hasText(employee.getEmail())) {
            throw AppException.badRequest("An email address is required to create a login for this employee.");
        }
        if (defaultPassword.length() < 8) {
            throw AppException.badRequest("Default password must be at least 8 characters");
        }
        if (userRepository.existsByEmail(employee.getEmail())) {
            throw AppException.conflict("Email already registered: " + employee.getEmail());
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> AppException.notFound("Role not found: " + roleName));

        User user = new User();
        user.setTenantId(employee.getTenantId());
        user.setEmail(employee.getEmail());
        user.setFullName(employeeName(employee));
        user.setPassword(passwordEncoder.encode(defaultPassword));
        // A mutable copy on purpose. Hibernate wraps whichever instance it is handed, so a
        // Set.of(...) here becomes the backing store of the persistent collection, and the next
        // merge of this user fails on clear() with UnsupportedOperationException.
        user.setRoles(new java.util.HashSet<>(java.util.Set.of(role)));
        user.setStatus(User.UserStatus.PENDING_APPROVAL);
        user.setChangePasswordOnLogin(true);
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(TEMP_PASSWORD_VALID_DAYS));

        // Manager is optional — omitted for a top-level position (no one to approve it as a peer;
        // an admin approves it instead, see AuthService.requireApprover()).
        if (managerId != null) {
            Employee manager = employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(employee.getTenantId(), managerId)
                    .orElseThrow(() -> AppException.notFound("Manager employee not found: " + managerId));
            if (manager.getUserId() == null) {
                throw AppException.businessRule(
                        "The selected manager doesn't have a login of their own yet, so they can't approve this account.");
            }
            user.setReportingManagerId(manager.getUserId());
            user.setReportingManagerName(employeeName(manager));
        }
        return userRepository.save(user);
    }

    @Transactional
    public EmployeeDto update(UUID id, CreateEmployeeRequest request) {
        Employee employee = findOrThrow(id);
        applyRequest(employee, request);
        employee = employeeRepository.save(employee);
        deactivateLinkedUserIfLeft(employee);
        syncGroupsAndManagerToUser(employee);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.EMPLOYEE, employeeName(employee), employee.getId(), employee.getEmployeeCode());
        return toDto(employee);
    }

    @Transactional
    public EmployeeDto updateStatus(UUID id, EmployeeStatus status) {
        Employee employee = findOrThrow(id);
        employee.setStatus(status);
        employee = employeeRepository.save(employee);
        deactivateLinkedUserIfLeft(employee);
        return toDto(employee);
    }

    /**
     * Employee is the source of truth for Groups and Reporting Manager — this pushes both onto
     * the linked User login whenever the Employee is saved, so User Management and the Employee
     * screen never drift apart. Users with no linked employee (e.g. the initial admin account)
     * are edited directly on User Management with nothing to sync from.
     */
    private void syncGroupsAndManagerToUser(Employee employee) {
        if (employee.getUserId() == null) return;
        userRepository.findById(employee.getUserId()).ifPresent(user -> {
            user.setGroupIds(new java.util.HashSet<>(employee.getGroupIds()));
            if (!employee.getGroupIds().isEmpty()) {
                UUID primary = employee.getGroupIds().iterator().next();
                userGroupRepository.findById(primary).ifPresentOrElse(
                        g -> { user.setGroupId(g.getId()); user.setGroupName(g.getName()); },
                        () -> { user.setGroupId(null); user.setGroupName(null); });
            } else {
                user.setGroupId(null);
                user.setGroupName(null);
            }
            if (employee.getManagerId() != null) {
                employeeRepository.findById(employee.getManagerId()).ifPresent(manager -> {
                    if (manager.getUserId() != null) {
                        user.setReportingManagerId(manager.getUserId());
                        user.setReportingManagerName(employeeName(manager));
                    }
                });
            } else {
                user.setReportingManagerId(null);
                user.setReportingManagerName(null);
            }
            userRepository.save(user);
        });
    }

    /**
     * The "leaver" half of the login lifecycle: the moment an employee is marked resigned or
     * terminated, their login is deactivated too — an orphaned active account for someone no
     * longer with the company is exactly the kind of gap security audits flag. Deliberately
     * one-directional: moving status back to ACTIVE later (a rehire) does NOT auto-reactivate the
     * login, since that should be a deliberate re-provisioning decision, not automatic.
     */
    private void deactivateLinkedUserIfLeft(Employee employee) {
        if (employee.getUserId() == null) return;
        if (employee.getStatus() != EmployeeStatus.TERMINATED && employee.getStatus() != EmployeeStatus.RESIGNED) return;
        userRepository.findById(employee.getUserId()).ifPresent(user -> {
            if (user.getStatus() != User.UserStatus.INACTIVE) {
                user.setStatus(User.UserStatus.INACTIVE);
                userRepository.save(user);
                log.info("Deactivated login for {} — employee {} is now {}", user.getEmail(), employee.getId(), employee.getStatus());
            }
        });
    }

    @Transactional
    public void delete(UUID id) {
        Employee employee = findOrThrow(id);
        employee.setDeletedAt(LocalDateTime.now());
        employeeRepository.save(employee);
        log.info("Employee soft-deleted: id={}", id);
    }

    private void applyRequest(Employee e, CreateEmployeeRequest r) {
        e.setFirstName(r.getFirstName());
        e.setLastName(r.getLastName());
        e.setEmail(r.getEmail());
        e.setPhone(r.getPhone());
        e.setMobile(r.getMobile());
        e.setPhoto(r.getPhoto());
        e.setGender(r.getGender());
        e.setDateOfBirth(r.getDateOfBirth());
        e.setNationality(r.getNationality());
        e.setDesignation(r.getDesignation());
        e.setEmploymentType(r.getEmploymentType());
        e.setJoiningDate(r.getJoiningDate());
        e.setConfirmationDate(r.getConfirmationDate());
        e.setResignationDate(r.getResignationDate());
        if (r.getStatus() != null && !r.getStatus().isEmpty()) {
            e.setStatus(EmployeeStatus.valueOf(r.getStatus()));
        }
        e.setDepartmentId(r.getDepartmentId());
        e.setBranchId(r.getBranchId());
        e.setManagerId(r.getManagerId());
        e.setUserId(r.getUserId());
        if (r.getGroupIds() != null) e.setGroupIds(new java.util.HashSet<>(r.getGroupIds()));
        e.setBasicSalary(r.getBasicSalary());
        e.setAddress(r.getAddress());
        e.setDistrict(r.getDistrict());
        e.setCity(r.getCity());
        e.setState(r.getState());
        e.setCountry(r.getCountry());
        e.setBankName(r.getBankName());
        e.setBankAccount(r.getBankAccount());
        e.setBankIFSC(r.getBankIFSC());
        e.setPanNumber(r.getPanNumber());
        e.setPfNumber(r.getPfNumber());
        e.setEsiNumber(r.getEsiNumber());
        e.setTitle(r.getTitle());
        e.setFatherName(r.getFatherName());
        e.setFax(r.getFax());
        e.setPassportNumber(r.getPassportNumber());
        e.setQualification(r.getQualification());
        e.setPreviousCompany(r.getPreviousCompany());
        e.setExperienceYears(r.getExperienceYears());
        e.setAddress2(r.getAddress2());
        e.setZipCode(r.getZipCode());
        e.setTempAddress(r.getTempAddress());
        e.setTempAddress2(r.getTempAddress2());
        e.setTempDistrict(r.getTempDistrict());
        e.setTempCountry(r.getTempCountry());
        e.setTempCity(r.getTempCity());
        e.setTempState(r.getTempState());
        e.setTempZip(r.getTempZip());
    }

    private EmployeeDto toDto(Employee e) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(e.getId());
        dto.setTenantId(e.getTenantId());
        dto.setEmployeeCode(e.getEmployeeCode());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setMobile(e.getMobile());
        dto.setPhoto(e.getPhoto());
        dto.setGender(e.getGender());
        dto.setDateOfBirth(e.getDateOfBirth());
        dto.setDesignation(e.getDesignation());
        dto.setEmploymentType(e.getEmploymentType());
        dto.setJoiningDate(e.getJoiningDate());
        dto.setStatus(e.getStatus());
        dto.setDepartmentId(e.getDepartmentId());
        dto.setBranchId(e.getBranchId());
        dto.setManagerId(e.getManagerId());
        dto.setUserId(e.getUserId());
        dto.setGroupIds(e.getGroupIds());
        if (e.getUserId() != null) {
            userRepository.findById(e.getUserId()).ifPresent(u -> {
                dto.setUserStatus(u.getStatus());
                dto.setRoleName(u.getRoles().stream().findFirst().map(Role::getName).orElse(null));
            });
        }
        dto.setBasicSalary(e.getBasicSalary());
        dto.setAddress(e.getAddress());
        dto.setDistrict(e.getDistrict());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setCountry(e.getCountry());
        dto.setNationality(e.getNationality());
        dto.setConfirmationDate(e.getConfirmationDate());
        dto.setResignationDate(e.getResignationDate());
        dto.setBankName(e.getBankName());
        dto.setBankAccount(e.getBankAccount());
        dto.setBankIFSC(e.getBankIFSC());
        dto.setPanNumber(e.getPanNumber());
        dto.setPfNumber(e.getPfNumber());
        dto.setEsiNumber(e.getEsiNumber());
        dto.setTitle(e.getTitle());
        dto.setFatherName(e.getFatherName());
        dto.setFax(e.getFax());
        dto.setPassportNumber(e.getPassportNumber());
        dto.setQualification(e.getQualification());
        dto.setPreviousCompany(e.getPreviousCompany());
        dto.setExperienceYears(e.getExperienceYears());
        dto.setAddress2(e.getAddress2());
        dto.setZipCode(e.getZipCode());
        dto.setTempAddress(e.getTempAddress());
        dto.setTempAddress2(e.getTempAddress2());
        dto.setTempDistrict(e.getTempDistrict());
        dto.setTempCountry(e.getTempCountry());
        dto.setTempCity(e.getTempCity());
        dto.setTempState(e.getTempState());
        dto.setTempZip(e.getTempZip());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private Employee findOrThrow(UUID id) {
        return employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Employee not found: " + id));
    }
}
