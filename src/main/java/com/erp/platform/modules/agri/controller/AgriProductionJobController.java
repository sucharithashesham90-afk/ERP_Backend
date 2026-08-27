package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProductionJobRequest;
import com.erp.platform.modules.agri.dto.ProductionJobDto;
import com.erp.platform.modules.agri.service.AgriProductionJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/production-jobs")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AgriProductionJobController {

    private final AgriProductionJobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductionJobDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(jobService.list(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionJobDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductionJobDto>> create(@RequestBody CreateProductionJobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(jobService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductionJobDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateProductionJobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(jobService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        jobService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
