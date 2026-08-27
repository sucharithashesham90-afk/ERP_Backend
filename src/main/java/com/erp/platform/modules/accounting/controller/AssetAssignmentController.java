package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AssetAssignment;
import com.erp.platform.modules.accounting.repository.AssetAssignmentRepository;
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
@RequestMapping("/api/v1/accounting/asset-assignments")
@RequiredArgsConstructor
@Tag(name = "Accounting - Asset Assignments", description = "Asset assignment to employees")
public class AssetAssignmentController {

    private final AssetAssignmentRepository assetAssignmentRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List asset assignments")
    public ResponseEntity<ApiResponse<List<AssetAssignment>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                assetAssignmentRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get asset assignment by ID")
    public ResponseEntity<ApiResponse<AssetAssignment>> getById(@PathVariable UUID id) {
        AssetAssignment entity = assetAssignmentRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Asset assignment not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create asset assignment")
    public ResponseEntity<ApiResponse<AssetAssignment>> create(@RequestBody AssetAssignment req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.ok(ApiResponse.success(assetAssignmentRepository.save(req), "Assigned"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update asset assignment")
    public ResponseEntity<ApiResponse<AssetAssignment>> update(@PathVariable UUID id, @RequestBody AssetAssignment req) {
        AssetAssignment e = assetAssignmentRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Asset assignment not found: " + id));
        e.setAssetName(req.getAssetName());
        e.setEmployeeName(req.getEmployeeName());
        e.setIssueDate(req.getIssueDate());
        e.setReturnDate(req.getReturnDate());
        e.setStatus(req.getStatus());
        e.setRemarks(req.getRemarks());
        return ResponseEntity.ok(ApiResponse.success(assetAssignmentRepository.save(e), "Updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete asset assignment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        AssetAssignment e = assetAssignmentRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Asset assignment not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        assetAssignmentRepository.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
