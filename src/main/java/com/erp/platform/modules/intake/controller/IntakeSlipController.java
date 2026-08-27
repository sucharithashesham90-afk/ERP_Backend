package com.erp.platform.modules.intake.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.intake.dto.CreateIntakeSlipRequest;
import com.erp.platform.modules.intake.dto.IntakeSlipDto;
import com.erp.platform.modules.intake.entity.IntakeSlip.SlipStatus;
import com.erp.platform.modules.intake.service.IntakeSlipService;
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
@RequestMapping("/api/v1/intake/slips")
@RequiredArgsConstructor
@Tag(name = "Material Intake - Slips", description = "Intake slip management")
public class IntakeSlipController {

    private final IntakeSlipService intakeSlipService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List intake slips")
    public ResponseEntity<ApiResponse<PageResponse<IntakeSlipDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(intakeSlipService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get intake slip by ID")
    public ResponseEntity<ApiResponse<IntakeSlipDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(intakeSlipService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create intake slip")
    public ResponseEntity<ApiResponse<IntakeSlipDto>> create(
            @Valid @RequestBody CreateIntakeSlipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(intakeSlipService.create(request), "Intake slip created successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update intake slip status")
    public ResponseEntity<ApiResponse<IntakeSlipDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        SlipStatus status = SlipStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(intakeSlipService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete intake slip (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        intakeSlipService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Intake slip deleted successfully"));
    }
}
