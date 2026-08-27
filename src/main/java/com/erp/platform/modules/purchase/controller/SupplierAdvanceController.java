package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.entity.AdvanceAdjustment;
import com.erp.platform.modules.purchase.entity.SupplierAdvance;
import com.erp.platform.modules.purchase.service.SupplierAdvanceService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/supplier-advances")
@RequiredArgsConstructor
@Tag(name = "Purchase - Supplier Advances", description = "Supplier advance management")
public class SupplierAdvanceController {

    private final SupplierAdvanceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List supplier advances")
    public ResponseEntity<ApiResponse<PageResponse<SupplierAdvance>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(vendorId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get supplier advance by ID")
    public ResponseEntity<ApiResponse<SupplierAdvance>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create supplier advance")
    public ResponseEntity<ApiResponse<SupplierAdvance>> create(@RequestBody SupplierAdvance req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update supplier advance")
    public ResponseEntity<ApiResponse<SupplierAdvance>> update(@PathVariable UUID id, @RequestBody SupplierAdvance req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel supplier advance")
    public ResponseEntity<ApiResponse<SupplierAdvance>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancel(id), "Cancelled"));
    }

    @PostMapping("/{id}/adjustments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Adjust supplier advance")
    public ResponseEntity<ApiResponse<SupplierAdvance>> adjust(@PathVariable UUID id, @RequestBody AdvanceAdjustment adjustment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.adjust(id, adjustment), "Adjusted"));
    }

    @GetMapping("/{id}/adjustments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get adjustments for supplier advance")
    public ResponseEntity<ApiResponse<List<AdvanceAdjustment>>> getAdjustments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getAdjustments(id)));
    }
}
