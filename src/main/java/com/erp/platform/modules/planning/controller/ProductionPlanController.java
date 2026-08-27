package com.erp.platform.modules.planning.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.planning.dto.CreateProductionPlanRequest;
import com.erp.platform.modules.planning.dto.ProductionPlanDto;
import com.erp.platform.modules.planning.entity.ProductionPlan.PlanStatus;
import com.erp.platform.modules.planning.service.ProductionPlanService;
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
@RequestMapping("/api/v1/planning/plans")
@RequiredArgsConstructor
@Tag(name = "Production Planning - Plans", description = "Production plan management")
public class ProductionPlanController {

    private final ProductionPlanService productionPlanService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List production plans")
    public ResponseEntity<ApiResponse<PageResponse<ProductionPlanDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) PlanStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(productionPlanService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get production plan by ID")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productionPlanService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production plan")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> create(
            @Valid @RequestBody CreateProductionPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productionPlanService.create(request), "Production plan created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production plan")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productionPlanService.update(id, request), "Production plan updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production plan status")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        PlanStatus status = PlanStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(productionPlanService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete production plan (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productionPlanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Production plan deleted successfully"));
    }
}
