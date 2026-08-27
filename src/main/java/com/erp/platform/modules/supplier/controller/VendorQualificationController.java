package com.erp.platform.modules.supplier.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.supplier.dto.CreateVendorQualificationRequest;
import com.erp.platform.modules.supplier.entity.VendorQualification;
import com.erp.platform.modules.supplier.entity.VendorQualification.QualStatus;
import com.erp.platform.modules.supplier.service.VendorQualificationService;
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
@RequestMapping("/api/v1/supplier/qualifications")
@RequiredArgsConstructor
@Tag(name = "Supplier - Vendor Qualification", description = "Vendor qualification and approval workflow")
public class VendorQualificationController {

    private final VendorQualificationService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List vendor qualifications")
    public ResponseEntity<ApiResponse<PageResponse<VendorQualification>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        QualStatus st = status != null ? QualStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.success(service.list(st, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VendorQualification>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit vendor for qualification")
    public ResponseEntity<ApiResponse<VendorQualification>> create(@Valid @RequestBody CreateVendorQualificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Qualification submitted"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VendorQualification>> update(@PathVariable UUID id, @Valid @RequestBody CreateVendorQualificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve vendor qualification")
    public ResponseEntity<ApiResponse<VendorQualification>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(ApiResponse.success(service.approve(id, notes), "Vendor approved"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject vendor qualification")
    public ResponseEntity<ApiResponse<VendorQualification>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(ApiResponse.success(service.reject(id, notes), "Vendor rejected"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
