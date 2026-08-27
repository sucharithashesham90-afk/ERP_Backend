package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateSalesPlanRequest;
import com.erp.platform.modules.sales.dto.SalesPlanDto;
import com.erp.platform.modules.sales.entity.SalesPlan.PlanStatus;
import com.erp.platform.modules.sales.service.SalesPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/plans")
@RequiredArgsConstructor
@Tag(name = "Sales Plans", description = "Sales planning management")
public class SalesPlanController {

    private final SalesPlanService salesPlanService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales plans")
    public ResponseEntity<ApiResponse<PageResponse<SalesPlanDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(defaultValue = "0") int planYear,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.list(status, planYear, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sales plan by ID")
    public ResponseEntity<ApiResponse<SalesPlanDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new sales plan")
    public ResponseEntity<ApiResponse<SalesPlanDto>> create(@Valid @RequestBody CreateSalesPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(salesPlanService.create(request), "Sales plan created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales plan")
    public ResponseEntity<ApiResponse<SalesPlanDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSalesPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.update(id, request), "Sales plan updated successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve sales plan")
    public ResponseEntity<ApiResponse<SalesPlanDto>> approve(
            @PathVariable UUID id,
            @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.approve(id, request.getApprovedBy()), "Sales plan approved"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Activate sales plan")
    public ResponseEntity<ApiResponse<SalesPlanDto>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.activate(id), "Sales plan activated"));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close sales plan")
    public ResponseEntity<ApiResponse<SalesPlanDto>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesPlanService.close(id), "Sales plan closed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete sales plan (soft delete, DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        salesPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sales plan deleted successfully"));
    }

    @Data
    public static class ApproveRequest {
        private String approvedBy;
    }
}
