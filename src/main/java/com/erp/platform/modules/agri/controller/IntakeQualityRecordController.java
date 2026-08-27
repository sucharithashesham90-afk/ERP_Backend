package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateIntakeQualityRecordRequest;
import com.erp.platform.modules.agri.dto.IntakeQualityRecordDto;
import com.erp.platform.modules.agri.service.IntakeQualityRecordService;
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
@RequestMapping("/api/v1/agri/intake-quality-records")
@RequiredArgsConstructor
@Tag(name = "Intake Quality Records", description = "Quality parameters recorded at intake")
public class IntakeQualityRecordController {

    private final IntakeQualityRecordService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List intake quality records")
    public ResponseEntity<ApiResponse<PageResponse<IntakeQualityRecordDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get intake quality record by ID")
    public ResponseEntity<ApiResponse<IntakeQualityRecordDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create intake quality record")
    public ResponseEntity<ApiResponse<IntakeQualityRecordDto>> create(@RequestBody CreateIntakeQualityRecordRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Intake quality record created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update intake quality record")
    public ResponseEntity<ApiResponse<IntakeQualityRecordDto>> update(@PathVariable UUID id,
            @RequestBody CreateIntakeQualityRecordRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete intake quality record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Intake quality record deleted"));
    }
}
