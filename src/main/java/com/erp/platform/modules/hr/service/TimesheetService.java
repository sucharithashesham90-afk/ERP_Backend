package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Timesheet;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Timesheets, scoped to who may see them.
 *
 * <p>Everyone's for HR; your own for anybody else. A timesheet says where a person was and for how
 * long, so it is treated like the rest of the personal data in this module: filtered in the query,
 * never loaded and then hidden.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimesheetService {

    private final TimesheetRepository repo;
    private final EmployeeRepository employeeRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;

    public PageResponse<Timesheet> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(hrAccess.isHr()
                ? repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : repo.findByTenantIdAndEmployeeIdAndDeletedAtIsNull(
                        tenantId, hrAccess.requireCurrentEmployeeId(), pageable));
    }

    public Timesheet getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public Timesheet create(Timesheet req) {
        UUID tenantId = tenantContext.current();
        // Anyone who is not HR records their own time, whatever the request says. Logging hours
        // against a colleague is not a thing an employee should be able to do by editing a payload.
        UUID employeeId = hrAccess.isHr() && req.getEmployeeId() != null
                ? req.getEmployeeId()
                : hrAccess.requireCurrentEmployeeId();
        validate(req);

        Timesheet e = new Timesheet();
        e.setTenantId(tenantId);
        e.setEmployeeId(employeeId);
        e.setEmployeeName(nameOf(tenantId, employeeId, req.getEmployeeName()));
        e.setWorkDate(req.getWorkDate() != null ? req.getWorkDate() : LocalDate.now());
        e.setHours(req.getHours());
        e.setActivityType(req.getActivityType() == null || req.getActivityType().isBlank()
                ? "WORK" : req.getActivityType());
        e.setDescription(req.getDescription());
        e.setStatus("DRAFT");
        return repo.save(e);
    }

    @Transactional
    public Timesheet update(UUID id, Timesheet req) {
        Timesheet e = findOrThrow(id);
        if (!"DRAFT".equals(e.getStatus()) && !"REJECTED".equals(e.getStatus()) && !hrAccess.isHr()) {
            throw AppException.businessRule("This entry has been submitted and can no longer be edited.");
        }
        validate(req);
        if (req.getWorkDate() != null) e.setWorkDate(req.getWorkDate());
        e.setHours(req.getHours());
        if (req.getActivityType() != null && !req.getActivityType().isBlank()) e.setActivityType(req.getActivityType());
        e.setDescription(req.getDescription());
        return repo.save(e);
    }

    @Transactional
    public Timesheet submit(UUID id) {
        Timesheet e = findOrThrow(id);
        if ("APPROVED".equals(e.getStatus())) throw AppException.businessRule("This entry is already approved.");
        e.setStatus("SUBMITTED");
        e.setRejectionReason(null);
        return repo.save(e);
    }

    @Transactional
    public Timesheet approve(UUID id) {
        hrAccess.assertHr();
        Timesheet e = findOrThrow(id);
        e.setStatus("APPROVED");
        e.setRejectionReason(null);
        return repo.save(e);
    }

    @Transactional
    public Timesheet reject(UUID id, String reason) {
        hrAccess.assertHr();
        if (reason == null || reason.isBlank()) throw AppException.badRequest("A rejection reason is required");
        Timesheet e = findOrThrow(id);
        e.setStatus("REJECTED");
        e.setRejectionReason(reason);
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        Timesheet e = findOrThrow(id);
        if ("APPROVED".equals(e.getStatus()) && !hrAccess.isHr()) {
            throw AppException.businessRule("An approved entry can only be removed by HR.");
        }
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    private void validate(Timesheet req) {
        if (req.getHours() == null || req.getHours().compareTo(BigDecimal.ZERO) <= 0)
            throw AppException.badRequest("Hours must be greater than zero");
        if (req.getHours().compareTo(new BigDecimal("24")) > 0)
            throw AppException.badRequest("A day cannot hold more than 24 hours");
        if (req.getWorkDate() != null && req.getWorkDate().isAfter(LocalDate.now()))
            throw AppException.badRequest("Time cannot be logged against a future date");
    }

    private String nameOf(UUID tenantId, UUID employeeId, String fallback) {
        return employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, employeeId)
                .map(this::displayName).orElse(fallback);
    }

    private String displayName(Employee e) {
        String first = e.getFirstName() == null ? "" : e.getFirstName().trim();
        String last = e.getLastName() == null ? "" : e.getLastName().trim();
        return (first + " " + last).trim();
    }

    /** Every path in and out goes through here, so no route can skip the check. */
    private Timesheet findOrThrow(UUID id) {
        Timesheet e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Timesheet entry not found: " + id));
        hrAccess.assertCanSee(e.getEmployeeId());
        return e;
    }
}
