package com.erp.platform.modules.planning.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.planning.entity.PlanPeriod;
import com.erp.platform.modules.planning.repository.PlanPeriodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning/periods")
@RequiredArgsConstructor
@Tag(name = "Planning - Plan Periods", description = "Plan period definition")
public class PlanPeriodController {

    private final PlanPeriodRepository planPeriodRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List plan periods")
    public ResponseEntity<ApiResponse<List<PlanPeriod>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                planPeriodRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get plan period by ID")
    public ResponseEntity<ApiResponse<PlanPeriod>> getById(@PathVariable UUID id) {
        PlanPeriod entity = planPeriodRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plan period not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create plan period")
    public ResponseEntity<ApiResponse<PlanPeriod>> create(@RequestBody PlanPeriod req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.ok(ApiResponse.success(planPeriodRepository.save(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update plan period")
    public ResponseEntity<ApiResponse<PlanPeriod>> update(@PathVariable UUID id, @RequestBody PlanPeriod req) {
        PlanPeriod e = planPeriodRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plan period not found: " + id));
        e.setPeriodName(req.getPeriodName());
        e.setFromDate(req.getFromDate());
        e.setToDate(req.getToDate());
        e.setPlanType(req.getPlanType());
        e.setStatus(req.getStatus());
        return ResponseEntity.ok(ApiResponse.success(planPeriodRepository.save(e), "Updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete plan period")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        PlanPeriod e = planPeriodRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plan period not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        planPeriodRepository.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
