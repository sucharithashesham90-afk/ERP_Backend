package com.erp.platform.modules.intake.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.intake.dto.CreateIntakeScheduleRequest;
import com.erp.platform.modules.intake.dto.IntakeScheduleDto;
import com.erp.platform.modules.intake.entity.IntakeSchedule.ScheduleStatus;
import com.erp.platform.modules.intake.service.IntakeScheduleService;
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
@RequestMapping("/api/v1/intake/schedules")
@RequiredArgsConstructor
@Tag(name = "Material Intake - Schedules", description = "Intake schedule management")
public class IntakeScheduleController {

    private final IntakeScheduleService intakeScheduleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List intake schedules")
    public ResponseEntity<ApiResponse<PageResponse<IntakeScheduleDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ScheduleStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(intakeScheduleService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get intake schedule by ID")
    public ResponseEntity<ApiResponse<IntakeScheduleDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(intakeScheduleService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create intake schedule")
    public ResponseEntity<ApiResponse<IntakeScheduleDto>> create(
            @Valid @RequestBody CreateIntakeScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(intakeScheduleService.create(request), "Intake schedule created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update intake schedule (DRAFT only)")
    public ResponseEntity<ApiResponse<IntakeScheduleDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateIntakeScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(intakeScheduleService.update(id, request), "Intake schedule updated"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update intake schedule status")
    public ResponseEntity<ApiResponse<IntakeScheduleDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        ScheduleStatus status = ScheduleStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(intakeScheduleService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete intake schedule (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        intakeScheduleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Intake schedule deleted successfully"));
    }
}
