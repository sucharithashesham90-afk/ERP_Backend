package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.CostCenter;
import com.erp.platform.modules.accounting.entity.CostCenterAllocation;
import com.erp.platform.modules.accounting.service.CostCenterService;
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
@RequiredArgsConstructor
@Tag(name = "Accounting - Cost Centers", description = "Cost center accounting management")
public class CostCenterController {

    private final CostCenterService service;

    @GetMapping("/api/v1/accounting/cost-centers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List cost centers")
    public ResponseEntity<ApiResponse<PageResponse<CostCenter>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/accounting/cost-centers/all-active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active cost centers for dropdown")
    public ResponseEntity<ApiResponse<List<CostCenter>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActive()));
    }

    @GetMapping("/api/v1/accounting/cost-centers/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cost center by id")
    public ResponseEntity<ApiResponse<CostCenter>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping("/api/v1/accounting/cost-centers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create cost center")
    public ResponseEntity<ApiResponse<CostCenter>> create(@RequestBody CostCenter req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/api/v1/accounting/cost-centers/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cost center")
    public ResponseEntity<ApiResponse<CostCenter>> update(@PathVariable UUID id, @RequestBody CostCenter req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/cost-centers/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete cost center")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/api/v1/accounting/cost-centers/{id}/allocations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get allocations for a cost center")
    public ResponseEntity<ApiResponse<PageResponse<CostCenterAllocation>>> getAllocations(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getAllocations(id, PageRequest.of(page, size))));
    }

    @PostMapping("/api/v1/accounting/cost-centers/{id}/allocations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add allocation to a cost center")
    public ResponseEntity<ApiResponse<CostCenterAllocation>> addAllocation(
            @PathVariable UUID id, @RequestBody CostCenterAllocation alloc) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(service.addAllocation(id, alloc), "Allocation added"));
    }

    @GetMapping("/api/v1/accounting/cost-centers/{id}/budget-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get budget summary for a cost center")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBudgetSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getBudgetSummary(id)));
    }
}
