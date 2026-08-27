package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateLeaveRequest;
import com.erp.platform.modules.hr.dto.LeaveApplicationDto;
import com.erp.platform.modules.hr.service.LeaveApplicationService;
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
@RequestMapping("/api/v1/hr/leave-applications")
@RequiredArgsConstructor
@Tag(name = "HR - Leave Applications", description = "Leave application management")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List leave applications")
    public ResponseEntity<ApiResponse<PageResponse<LeaveApplicationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Scoped: HR sees every application, anyone else sees only their own.
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(leaveService.listVisible(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> apply(@RequestBody CreateLeaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leaveService.apply(request), "Leave application submitted"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.approve(id), "Leave approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(leaveService.reject(id, reason), "Leave rejected"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a leave application")
    public ResponseEntity<ApiResponse<LeaveApplicationDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.cancel(id), "Leave cancelled"));
    }

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get approved leave days by type for an employee")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> leaveBalance(
            @RequestParam UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveBalance(employeeId)));
    }
}
