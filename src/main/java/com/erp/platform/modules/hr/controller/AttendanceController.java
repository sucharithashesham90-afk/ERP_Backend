package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.AttendanceDto;
import com.erp.platform.modules.hr.dto.CreateAttendanceRequest;
import com.erp.platform.modules.hr.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/attendance")
@RequiredArgsConstructor
@Tag(name = "HR - Attendance", description = "Employee attendance management")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List attendance records")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(ApiResponse.success(attendanceService.list(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark attendance")
    public ResponseEntity<ApiResponse<AttendanceDto>> create(@RequestBody CreateAttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attendanceService.create(request), "Attendance marked"));
    }

    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Today's attendance record for an employee (null if not punched in yet)")
    public ResponseEntity<ApiResponse<AttendanceDto>> today(@RequestParam UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getToday(employeeId)));
    }

    @PostMapping("/punch-in")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Punch in for today (workMode OFFICE or WFH)")
    public ResponseEntity<ApiResponse<AttendanceDto>> punchIn(@RequestBody Map<String, Object> body) {
        UUID employeeId = UUID.fromString(String.valueOf(body.get("employeeId")));
        String workMode = body.get("workMode") != null ? String.valueOf(body.get("workMode")) : null;
        return ResponseEntity.ok(ApiResponse.success(attendanceService.punchIn(employeeId, workMode), "Punched in"));
    }

    @PostMapping("/punch-out")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Punch out for today")
    public ResponseEntity<ApiResponse<AttendanceDto>> punchOut(@RequestBody Map<String, Object> body) {
        UUID employeeId = UUID.fromString(String.valueOf(body.get("employeeId")));
        return ResponseEntity.ok(ApiResponse.success(attendanceService.punchOut(employeeId), "Punched out"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update attendance record")
    public ResponseEntity<ApiResponse<AttendanceDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateAttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete attendance record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        attendanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/monthly-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Monthly attendance summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> monthlySummary(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
