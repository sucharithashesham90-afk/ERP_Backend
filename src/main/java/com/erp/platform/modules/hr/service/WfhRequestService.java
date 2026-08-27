package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.WfhRequest;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.repository.WfhRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Work-from-home requests, scoped to who may see and action them.
 *
 * <p>This lived in the controller, reading its repository directly, and that is exactly why it was
 * missed when every other personal-data endpoint was scoped: an audit that follows services never
 * reaches a controller holding its own repository. Every request now goes through one service, so
 * there is a single place to look and a single place to get it wrong.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WfhRequestService {

    private final WfhRequestRepository repo;
    private final EmployeeRepository employeeRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;

    /** Everyone's requests for HR; your own for anybody else. */
    public PageResponse<WfhRequest> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(hrAccess.isHr()
                ? repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : repo.findByTenantIdAndEmployeeIdAndDeletedAtIsNull(
                        tenantId, hrAccess.requireCurrentEmployeeId(), pageable));
    }

    @Transactional
    public WfhRequest apply(WfhRequest req) {
        if (req.getEmployeeId() == null) throw AppException.badRequest("Employee is required");
        if (req.getFromDate() == null || req.getToDate() == null)
            throw AppException.badRequest("From date and to date are required");
        req.setId(null);
        req.setTenantId(tenantContext.current());
        req.setDays((int) ChronoUnit.DAYS.between(req.getFromDate(), req.getToDate()) + 1);
        req.setStatus("PENDING");
        req.setApprovedBy(null);
        req.setRejectionReason(null);
        return repo.save(req);
    }

    @Transactional
    public WfhRequest approve(UUID id) {
        WfhRequest e = findOrThrow(id);
        requireApprover(e);
        if (!"PENDING".equals(e.getStatus())) throw AppException.badRequest("Only PENDING requests can be approved");
        e.setStatus("APPROVED");
        return repo.save(e);
    }

    @Transactional
    public WfhRequest reject(UUID id, String reason) {
        WfhRequest e = findOrThrow(id);
        requireApprover(e);
        if (!"PENDING".equals(e.getStatus())) throw AppException.badRequest("Only PENDING requests can be rejected");
        e.setStatus("REJECTED");
        e.setRejectionReason(reason);
        return repo.save(e);
    }

    @Transactional
    public WfhRequest cancel(UUID id) {
        WfhRequest e = findOrThrow(id);
        if ("APPROVED".equals(e.getStatus()) || "REJECTED".equals(e.getStatus()))
            throw AppException.badRequest("Cannot cancel an already processed request");
        e.setStatus("CANCELLED");
        return repo.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        WfhRequest e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    /** Every path in and out goes through here, so no route can skip the check. */
    private WfhRequest findOrThrow(UUID id) {
        WfhRequest request = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("WFH request not found: " + id));
        if (!hrAccess.isHr() && !isOwnOrReport(request)) {
            throw AppException.forbidden("You can only view your own records.");
        }
        return request;
    }

    /** Your own request, or one belonging to somebody who reports to you. */
    private boolean isOwnOrReport(WfhRequest request) {
        UUID caller = hrAccess.currentEmployee().map(Employee::getId).orElse(null);
        if (caller == null || request.getEmployeeId() == null) return false;
        if (request.getEmployeeId().equals(caller)) return true;
        return employeeRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), request.getEmployeeId())
                .map(applicant -> caller.equals(applicant.getManagerId()))
                .orElse(false);
    }

    /**
     * Who may approve or reject. HR may act on anyone's; otherwise it must be the applicant's
     * reporting manager, and an applicant with no manager can only be actioned by HR — nobody
     * approves their own work-from-home.
     */
    private void requireApprover(WfhRequest request) {
        if (hrAccess.isHr()) return;
        UUID caller = hrAccess.currentEmployee().map(Employee::getId).orElse(null);
        UUID managerId = request.getEmployeeId() == null ? null : employeeRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), request.getEmployeeId())
                .map(Employee::getManagerId).orElse(null);
        if (managerId == null) {
            throw AppException.forbidden(
                    "This employee has no reporting manager assigned, so only HR can approve or reject this request.");
        }
        if (!managerId.equals(caller)) {
            throw AppException.forbidden("Only this employee's reporting manager can approve or reject this request.");
        }
    }
}
