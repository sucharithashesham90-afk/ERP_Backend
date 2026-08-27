package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.entity.QualityInspection;
import com.erp.platform.modules.quality.service.QualityInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quality/inspections")
@RequiredArgsConstructor
@Tag(name = "Quality - Inspections", description = "Quality inspection management")
public class QualityInspectionController {

    private final QualityInspectionService inspectionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List quality inspections")
    public ResponseEntity<ApiResponse<PageResponse<QualityInspection>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(inspectionService.list(type, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create quality inspection")
    public ResponseEntity<ApiResponse<QualityInspection>> create(@RequestBody QualityInspection request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inspectionService.create(request), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quality inspection")
    public ResponseEntity<ApiResponse<QualityInspection>> update(
            @PathVariable UUID id, @RequestBody QualityInspection request) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete quality inspection")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        inspectionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
