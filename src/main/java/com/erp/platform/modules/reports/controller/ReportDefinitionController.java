package com.erp.platform.modules.reports.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.reports.dto.CreateReportDefinitionRequest;
import com.erp.platform.modules.reports.dto.CreateReportScheduleRequest;
import com.erp.platform.modules.reports.dto.ReportDefinitionDto;
import com.erp.platform.modules.reports.dto.ReportRunDto;
import com.erp.platform.modules.reports.dto.ReportScheduleDto;
import com.erp.platform.modules.reports.entity.ReportDefinition.ReportCategory;
import com.erp.platform.modules.reports.service.ReportDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/reports/definitions")
@RequiredArgsConstructor
@Tag(name = "Reports - Definitions", description = "Report definition management")
public class ReportDefinitionController {

    private final ReportDefinitionService reportDefinitionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List report definitions")
    public ResponseEntity<ApiResponse<PageResponse<ReportDefinitionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReportCategory category) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(reportDefinitionService.list(category, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get report definition by ID")
    public ResponseEntity<ApiResponse<ReportDefinitionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reportDefinitionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create report definition")
    public ResponseEntity<ApiResponse<ReportDefinitionDto>> create(@Valid @RequestBody CreateReportDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reportDefinitionService.create(request), "Report definition created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update report definition")
    public ResponseEntity<ApiResponse<ReportDefinitionDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateReportDefinitionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reportDefinitionService.update(id, request), "Report definition updated"));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Duplicate report definition")
    public ResponseEntity<ApiResponse<ReportDefinitionDto>> duplicate(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reportDefinitionService.duplicate(id), "Report definition duplicated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete report definition (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        reportDefinitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Report definition deleted successfully"));
    }

    @PostMapping("/{id}/schedules")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add schedule to report")
    public ResponseEntity<ApiResponse<ReportScheduleDto>> addSchedule(
            @PathVariable UUID id,
            @RequestBody CreateReportScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reportDefinitionService.addSchedule(id, request), "Schedule added successfully"));
    }

    @GetMapping("/{id}/runs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List run history for report (last 50)")
    public ResponseEntity<ApiResponse<PageResponse<ReportRunDto>>> listRuns(@PathVariable UUID id) {
        var pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "runAt"));
        return ResponseEntity.ok(ApiResponse.success(reportDefinitionService.listRuns(id, pageable)));
    }

    @PostMapping("/{id}/run")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record a report run")
    public ResponseEntity<ApiResponse<Void>> recordRun(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        String runBy = (String) body.get("runBy");
        String filtersJson = (String) body.get("filtersJson");
        int recordCount = body.containsKey("recordCount") ? Integer.parseInt(body.get("recordCount").toString()) : 0;
        long durationMs = body.containsKey("durationMs") ? Long.parseLong(body.get("durationMs").toString()) : 0L;
        boolean success = body.containsKey("success") ? Boolean.parseBoolean(body.get("success").toString()) : true;
        String error = (String) body.get("error");
        reportDefinitionService.recordRun(id, runBy, filtersJson, recordCount, durationMs, success, error);
        return ResponseEntity.ok(ApiResponse.success(null, "Run recorded successfully"));
    }
}
