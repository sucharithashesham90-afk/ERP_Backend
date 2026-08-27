package com.erp.platform.modules.planning.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.planning.dto.CreateProductionJobAllocationRequest;
import com.erp.platform.modules.planning.dto.ProductionJobAllocationDto;
import com.erp.platform.modules.planning.service.ProductionJobAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning/allocations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProductionJobAllocationController {

    private final ProductionJobAllocationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductionJobAllocationDto>>> listByJob(
            @RequestParam UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByJob(jobId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductionJobAllocationDto>> create(
            @Valid @RequestBody CreateProductionJobAllocationRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req)));
    }

    @PostMapping("/{id}/advance")
    public ResponseEntity<ApiResponse<ProductionJobAllocationDto>> payAdvance(
            @PathVariable UUID id,
            @RequestBody Map<String, BigDecimal> body) {
        BigDecimal amount = body.getOrDefault("amount", BigDecimal.ZERO);
        return ResponseEntity.ok(ApiResponse.success(service.payAdvance(id, amount)));
    }

    @PostMapping("/{id}/initiate")
    public ResponseEntity<ApiResponse<ProductionJobAllocationDto>> initiate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.initiate(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
