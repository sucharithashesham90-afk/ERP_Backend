package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.Timesheet;
import com.erp.platform.modules.hr.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/timesheets")
@RequiredArgsConstructor
@Tag(name = "HR - Timesheets", description = "Daily time recorded by an employee")
public class TimesheetController {

    private final TimesheetService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List timesheet entries")
    public ResponseEntity<ApiResponse<PageResponse<Timesheet>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("workDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Timesheet>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log time")
    public ResponseEntity<ApiResponse<Timesheet>> create(@RequestBody Timesheet req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Time logged"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Timesheet>> update(@PathVariable UUID id, @RequestBody Timesheet req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req), "Updated"));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit an entry for approval")
    public ResponseEntity<ApiResponse<Timesheet>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.submit(id), "Submitted"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve an entry (HR only)")
    public ResponseEntity<ApiResponse<Timesheet>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id), "Approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject an entry (HR only)")
    public ResponseEntity<ApiResponse<Timesheet>> reject(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                service.reject(id, body != null ? body.get("reason") : null), "Rejected"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
