package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.modules.hr.dto.CreateLeaveRequest;
import com.erp.platform.modules.hr.dto.LeaveApplicationDto;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.LeaveApplication;
import com.erp.platform.modules.hr.entity.LeaveApplication.LeaveStatus;
import com.erp.platform.modules.hr.repository.LeaveApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveRepository;
    private final HrAccessService hrAccess;
    private final com.erp.platform.modules.hr.repository.EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;
    private final TenantContext tenantContext;

    /**
     * Applications the signed-in user may see: everyone's for HR, their own for anybody else.
     *
     * <p>Filtered in the query rather than after it, so another employee's leave is never loaded,
     * never counted in the page total, and never one serialisation slip away from being returned.
     */
    public PageResponse<LeaveApplicationDto> listVisible(Pageable pageable) {
        if (hrAccess.isHr()) return list(pageable);
        UUID own = hrAccess.requireCurrentEmployeeId();
        return PageResponse.of(leaveRepository
                .findByTenantIdAndEmployeeIdAndDeletedAtIsNull(tenantContext.current(), own, pageable)
                .map(this::toDto));
    }

    public PageResponse<LeaveApplicationDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(leaveRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toDto));
    }

    @Transactional
    public LeaveApplicationDto apply(CreateLeaveRequest request) {
        UUID tenantId = tenantContext.current();
        UUID employeeId = request.resolveEmployeeId();
        if (employeeId == null) {
            throw AppException.badRequest("Employee is required");
        }
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw AppException.badRequest("From date and to date are required");
        }
        LeaveApplication application = new LeaveApplication();
        application.setTenantId(tenantId);
        application.setEmployeeId(employeeId);
        application.setLeaveType(request.getLeaveType());
        application.setLeaveTypeId(request.getLeaveTypeId());
        application.setFromDate(request.getFromDate());
        application.setToDate(request.getToDate());
        application.setDays(calculateDays(request.getFromDate(), request.getToDate()));
        application.setReason(request.getReason());
        application.setStatus(LeaveStatus.PENDING);
        return toDto(leaveRepository.save(application));
    }

    @Transactional
    public LeaveApplicationDto approve(UUID id) {
        LeaveApplication application = findOrThrow(id);
        if (application.getStatus() != LeaveStatus.PENDING) {
            throw AppException.badRequest("Only PENDING applications can be approved");
        }
        requireApprover(application);
        application.setStatus(LeaveStatus.APPROVED);
        return toDto(leaveRepository.save(application));
    }

    @Transactional
    public LeaveApplicationDto reject(UUID id, String reason) {
        LeaveApplication application = findOrThrow(id);
        if (application.getStatus() != LeaveStatus.PENDING) {
            throw AppException.badRequest("Only PENDING applications can be rejected");
        }
        requireApprover(application);
        application.setStatus(LeaveStatus.REJECTED);
        application.setRejectionReason(reason);
        return toDto(leaveRepository.save(application));
    }

    /**
     * Only the applicant's assigned reporting manager (Employee.managerId) may approve/reject —
     * same admin-fallback shape as AuthService.requireApprover() for employee login approval: no
     * manager assigned means no peer could ever approve it, so only an admin may act.
     */
    private void requireApprover(LeaveApplication application) {
        // HR can act on anyone's leave. Without this an HR user who is not the applicant's manager
        // was refused, which left an application stuck whenever the manager was away or unset.
        if (hrAccess.isHr()) return;
        UUID tenantId = tenantContext.current();
        Employee applicant = employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, application.getEmployeeId())
                .orElse(null);
        UUID managerId = applicant != null ? applicant.getManagerId() : null;
        UUID callerUserId = userContext.getUserId();
        if (managerId != null) {
            Employee callerEmployee = callerUserId != null
                    ? employeeRepository.findByTenantIdAndUserIdAndDeletedAtIsNull(tenantId, callerUserId).orElse(null)
                    : null;
            if (callerEmployee == null || !managerId.equals(callerEmployee.getId())) {
                throw AppException.forbidden("Only this employee's reporting manager can approve or reject their leave.");
            }
            return;
        }
        boolean callerIsAdmin = callerUserId != null && userRepository.findById(callerUserId)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()) || "TENANT_ADMIN".equals(r.getName())))
                .orElse(false);
        if (!callerIsAdmin) {
            throw AppException.forbidden("This employee has no reporting manager assigned — only an admin can approve or reject their leave.");
        }
    }

    @Transactional
    public LeaveApplicationDto cancel(UUID id) {
        LeaveApplication application = findOrThrow(id);
        if (application.getStatus() == LeaveStatus.APPROVED || application.getStatus() == LeaveStatus.REJECTED) {
            throw AppException.badRequest("Cannot cancel an already processed application");
        }
        application.setStatus(LeaveStatus.CANCELLED);
        return toDto(leaveRepository.save(application));
    }

    public List<Map<String, Object>> getLeaveBalance(UUID employeeId) {
        UUID tenantId = tenantContext.current();
        List<Object[]> rows = leaveRepository.sumApprovedLeaveByType(tenantId, employeeId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("leaveType", row[0]);
            entry.put("usedDays", ((Number) row[1]).intValue());
            result.add(entry);
        }
        return result;
    }

    private int calculateDays(LocalDate from, LocalDate to) {
        return (int) ChronoUnit.DAYS.between(from, to) + 1;
    }

    private LeaveApplication findOrThrow(UUID id) {
        return leaveRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Leave application not found: " + id));
    }

    private LeaveApplicationDto toDto(LeaveApplication a) {
        LeaveApplicationDto dto = new LeaveApplicationDto();
        dto.setId(a.getId());
        dto.setTenantId(a.getTenantId());
        dto.setEmployeeId(a.getEmployeeId());
        // Only the id was ever sent, so every consumer showed a blank where the person should be.
        if (a.getEmployeeId() != null) {
            employeeRepository.findById(a.getEmployeeId()).ifPresent(e ->
                    dto.setEmployeeName(((e.getFirstName() == null ? "" : e.getFirstName()) + " "
                            + (e.getLastName() == null ? "" : e.getLastName())).trim()));
        }
        dto.setLeaveType(a.getLeaveType());
        dto.setFromDate(a.getFromDate());
        dto.setToDate(a.getToDate());
        dto.setDays(a.getDays());
        dto.setReason(a.getReason());
        dto.setStatus(a.getStatus());
        dto.setApprovedBy(a.getApprovedBy());
        dto.setRejectionReason(a.getRejectionReason());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
