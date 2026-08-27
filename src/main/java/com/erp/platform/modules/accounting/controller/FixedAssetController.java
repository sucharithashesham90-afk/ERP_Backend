package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.AssetGroup;
import com.erp.platform.modules.accounting.entity.AssetMaintenance;
import com.erp.platform.modules.accounting.entity.DepreciationEntry;
import com.erp.platform.modules.accounting.entity.FixedAsset;
import com.erp.platform.modules.accounting.service.FixedAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accounting - Fixed Assets", description = "Fixed asset management")
public class FixedAssetController {

    private final FixedAssetService service;

    // ---- Asset Groups ----

    @GetMapping("/api/v1/accounting/asset-groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List asset groups")
    public ResponseEntity<ApiResponse<PageResponse<AssetGroup>>> listGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listGroups(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/accounting/asset-groups/all-active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active asset groups for dropdown")
    public ResponseEntity<ApiResponse<List<AssetGroup>>> getAllActiveGroups() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActiveGroups()));
    }

    @PostMapping("/api/v1/accounting/asset-groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create asset group")
    public ResponseEntity<ApiResponse<AssetGroup>> createGroup(@RequestBody AssetGroup req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createGroup(req), "Created"));
    }

    @PutMapping("/api/v1/accounting/asset-groups/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update asset group")
    public ResponseEntity<ApiResponse<AssetGroup>> updateGroup(@PathVariable UUID id, @RequestBody AssetGroup req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateGroup(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/asset-groups/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete asset group")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable UUID id) {
        service.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Fixed Assets ----

    @GetMapping("/api/v1/accounting/fixed-assets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List fixed assets")
    public ResponseEntity<ApiResponse<PageResponse<FixedAsset>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(status, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/accounting/fixed-assets/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get asset summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(service.getAssetSummary()));
    }

    @GetMapping("/api/v1/accounting/fixed-assets/pending-depreciations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pending depreciation entries")
    public ResponseEntity<ApiResponse<List<DepreciationEntry>>> getPendingDepreciations() {
        return ResponseEntity.ok(ApiResponse.success(service.getPendingDepreciations()));
    }

    @GetMapping("/api/v1/accounting/fixed-assets/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get fixed asset by id")
    public ResponseEntity<ApiResponse<FixedAsset>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping("/api/v1/accounting/fixed-assets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create fixed asset")
    public ResponseEntity<ApiResponse<FixedAsset>> create(@RequestBody FixedAsset req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/api/v1/accounting/fixed-assets/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update fixed asset")
    public ResponseEntity<ApiResponse<FixedAsset>> update(@PathVariable UUID id, @RequestBody FixedAsset req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/fixed-assets/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete fixed asset")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PatchMapping("/api/v1/accounting/fixed-assets/{id}/dispose")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dispose a fixed asset")
    public ResponseEntity<ApiResponse<FixedAsset>> dispose(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        LocalDate disposalDate = LocalDate.parse(body.get("disposalDate").toString());
        BigDecimal disposalAmount = new BigDecimal(body.getOrDefault("disposalAmount", "0").toString());
        return ResponseEntity.ok(ApiResponse.success(service.dispose(id, disposalDate, disposalAmount)));
    }

    @PostMapping("/api/v1/accounting/fixed-assets/{id}/depreciation-schedule")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate depreciation schedule for asset")
    public ResponseEntity<ApiResponse<List<DepreciationEntry>>> generateSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.generateDepreciationSchedule(id), "Schedule generated"));
    }

    @GetMapping("/api/v1/accounting/fixed-assets/{id}/depreciation-schedule")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get depreciation schedule for asset")
    public ResponseEntity<ApiResponse<List<DepreciationEntry>>> getSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getDepreciationSchedule(id)));
    }

    @PostMapping("/api/v1/accounting/depreciation-entries/{entryId}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post a depreciation entry to GL and update asset book value")
    public ResponseEntity<ApiResponse<DepreciationEntry>> postDepreciation(@PathVariable UUID entryId) {
        return ResponseEntity.ok(ApiResponse.success(service.postDepreciation(entryId),
                "Depreciation posted to GL — journal entry created (DRAFT)"));
    }

    @PostMapping("/api/v1/accounting/depreciation-entries/post-all-pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Batch-post all pending depreciation entries due on or before today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> postAllPending() {
        return ResponseEntity.ok(ApiResponse.success(service.postAllPendingDepreciations(),
                "Batch depreciation complete"));
    }

    // ── Asset Register Report ────────────────────────────────────────────────

    @GetMapping("/api/v1/accounting/fixed-assets/register")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Asset register — all assets grouped by asset group with cost / acc. depreciation / book value")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assetRegister(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(ApiResponse.success(service.getAssetRegister(status, location)));
    }

    // ── Depreciation Period Report ───────────────────────────────────────────

    @GetMapping("/api/v1/accounting/fixed-assets/depreciation-report")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Depreciation report for a given year (and optional month), grouped by asset group")
    public ResponseEntity<ApiResponse<Map<String, Object>>> depreciationReport(
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(ApiResponse.success(service.getDepreciationReport(year, month)));
    }

    // ── Asset Maintenance ────────────────────────────────────────────────────

    @GetMapping("/api/v1/accounting/asset-maintenance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List maintenance records — optionally filtered by assetId")
    public ResponseEntity<ApiResponse<PageResponse<AssetMaintenance>>> listMaintenance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID assetId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "maintenanceDate"));
        return ResponseEntity.ok(ApiResponse.success(service.listMaintenance(assetId, pageable)));
    }

    @PostMapping("/api/v1/accounting/asset-maintenance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log a new maintenance record")
    public ResponseEntity<ApiResponse<AssetMaintenance>> createMaintenance(@RequestBody AssetMaintenance req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createMaintenance(req), "Maintenance record created"));
    }

    @PutMapping("/api/v1/accounting/asset-maintenance/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a maintenance record")
    public ResponseEntity<ApiResponse<AssetMaintenance>> updateMaintenance(
            @PathVariable UUID id, @RequestBody AssetMaintenance req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateMaintenance(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/asset-maintenance/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete (soft) a maintenance record")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(@PathVariable UUID id) {
        service.deleteMaintenance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
