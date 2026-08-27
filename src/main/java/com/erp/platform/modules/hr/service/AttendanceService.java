package com.erp.platform.modules.hr.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.dto.AttendanceDto;
import com.erp.platform.modules.hr.dto.CreateAttendanceRequest;
import com.erp.platform.modules.hr.entity.Attendance;
import com.erp.platform.modules.hr.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;

    /** Everyone's attendance for HR; your own for anybody else. */
    public PageResponse<AttendanceDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (!hrAccess.isHr()) {
            UUID own = hrAccess.requireCurrentEmployeeId();
            return PageResponse.of(attendanceRepository
                    .findByTenantIdAndEmployeeIdAndDeletedAtIsNull(tenantId, own, pageable).map(this::toDto));
        }
        return PageResponse.of(attendanceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toDto));
    }

    @Transactional
    public AttendanceDto create(CreateAttendanceRequest request) {
        UUID tenantId = tenantContext.current();
        UUID employeeId = request.resolveEmployeeId();
        if (employeeId == null) {
            throw AppException.badRequest("Employee is required");
        }
        Attendance attendance = new Attendance();
        attendance.setTenantId(tenantId);
        applyRequest(attendance, request, employeeId);
        return toDto(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceDto update(UUID id, CreateAttendanceRequest request) {
        Attendance attendance = attendanceRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Attendance record not found: " + id));
        UUID employeeId = request.resolveEmployeeId();
        if (employeeId != null) attendance.setEmployeeId(employeeId);
        applyRequest(attendance, request, attendance.getEmployeeId());
        return toDto(attendanceRepository.save(attendance));
    }

    @Transactional
    public void delete(UUID id) {
        Attendance attendance = attendanceRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Attendance record not found: " + id));
        attendance.setDeletedAt(LocalDateTime.now());
        attendanceRepository.save(attendance);
    }

    /** Today's attendance record for an employee, or null if none has been punched yet. */
    public AttendanceDto getToday(UUID employeeId) {
        if (employeeId == null) throw AppException.badRequest("Employee is required");
        return attendanceRepository
                .findByTenantIdAndEmployeeIdAndDateAndDeletedAtIsNull(tenantContext.current(), employeeId, LocalDate.now())
                .map(this::toDto)
                .orElse(null);
    }

    /**
     * Records a punch-in for the employee for today. Creates today's record if absent; sets check-in
     * time (only the first punch-in of the day counts). workMode "WFH" marks a work-from-home day.
     */
    @Transactional
    public AttendanceDto punchIn(UUID employeeId, String workMode) {
        if (employeeId == null) throw AppException.badRequest("Employee is required");
        UUID tenantId = tenantContext.current();
        LocalDate today = LocalDate.now();
        Attendance a = attendanceRepository
                .findByTenantIdAndEmployeeIdAndDateAndDeletedAtIsNull(tenantId, employeeId, today)
                .orElseGet(() -> {
                    Attendance na = new Attendance();
                    na.setTenantId(tenantId);
                    na.setEmployeeId(employeeId);
                    na.setDate(today);
                    return na;
                });
        if (a.getCheckIn() == null) a.setCheckIn(LocalTime.now().withNano(0));
        a.setStatus("WFH".equalsIgnoreCase(workMode) ? "WFH" : "PRESENT");
        return toDto(attendanceRepository.save(a));
    }

    /** Records a punch-out for today and computes worked hours. Requires a prior punch-in. */
    @Transactional
    public AttendanceDto punchOut(UUID employeeId) {
        if (employeeId == null) throw AppException.badRequest("Employee is required");
        Attendance a = attendanceRepository
                .findByTenantIdAndEmployeeIdAndDateAndDeletedAtIsNull(tenantContext.current(), employeeId, LocalDate.now())
                .orElseThrow(() -> AppException.badRequest("Please punch in first"));
        a.setCheckOut(LocalTime.now().withNano(0));
        if (a.getCheckIn() != null) {
            double hours = a.getCheckOut().toSecondOfDay() / 3600.0 - a.getCheckIn().toSecondOfDay() / 3600.0;
            a.setWorkingHours(Math.max(0, Math.round(hours * 100) / 100.0));
        }
        return toDto(attendanceRepository.save(a));
    }

    private void applyRequest(Attendance a, CreateAttendanceRequest r, UUID employeeId) {
        a.setEmployeeId(employeeId);
        if (r.getDate() != null) a.setDate(r.getDate());
        if (r.getStatus() != null) a.setStatus(r.getStatus());
        a.setCheckIn(r.getCheckIn());
        a.setCheckOut(r.getCheckOut());
        a.setNotes(r.getNotes());
        if (r.getCheckIn() != null && r.getCheckOut() != null) {
            double hours = r.getCheckOut().toSecondOfDay() / 3600.0
                    - r.getCheckIn().toSecondOfDay() / 3600.0;
            a.setWorkingHours(Math.max(0, hours));
        }
    }

    private AttendanceDto toDto(Attendance a) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(a.getId());
        dto.setTenantId(a.getTenantId());
        dto.setEmployeeId(a.getEmployeeId());
        dto.setDate(a.getDate());
        dto.setStatus(a.getStatus());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setWorkingHours(a.getWorkingHours());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
