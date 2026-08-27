package com.erp.platform.modules.planning.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.planning.dto.CreateProductionJobRequest;
import com.erp.platform.modules.planning.dto.ProductionJobDto;
import com.erp.platform.modules.planning.entity.ProductionJob.JobStatus;
import com.erp.platform.modules.planning.service.ProductionJobService;
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
@RequestMapping("/api/v1/planning/jobs")
@RequiredArgsConstructor
@Tag(name = "Production Planning - Jobs", description = "Production job management")
public class ProductionJobController {

    private final ProductionJobService productionJobService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List production jobs")
    public ResponseEntity<ApiResponse<PageResponse<ProductionJobDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID planId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(productionJobService.list(planId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get production job by ID")
    public ResponseEntity<ApiResponse<ProductionJobDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productionJobService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production job")
    public ResponseEntity<ApiResponse<ProductionJobDto>> create(
            @Valid @RequestBody CreateProductionJobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productionJobService.create(request), "Production job created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production job (OPEN only)")
    public ResponseEntity<ApiResponse<ProductionJobDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductionJobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionJobService.update(id, request), "Production job updated"));
    }

    @PostMapping("/{id}/initiate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate production job: OPEN → IN_PROGRESS, creates production lot")
    public ResponseEntity<ApiResponse<ProductionJobDto>> initiate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productionJobService.initiate(id), "Production job initiated"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production job status")
    public ResponseEntity<ApiResponse<ProductionJobDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        JobStatus status = JobStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(productionJobService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete production job (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productionJobService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Production job deleted successfully"));
    }
}
