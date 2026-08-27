package com.erp.platform.modules.supplier.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.supplier.dto.CreateSupplierNonConformanceRequest;
import com.erp.platform.modules.supplier.dto.SupplierNonConformanceDto;
import com.erp.platform.modules.supplier.entity.SupplierNonConformance.NCStatus;
import com.erp.platform.modules.supplier.service.SupplierNonConformanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/supplier/nonconformances")
@RequiredArgsConstructor
@Tag(name = "Supplier - Non-Conformances")
public class SupplierNonConformanceController {

    private final SupplierNonConformanceService ncService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<SupplierNonConformanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) NCStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(ncService.list(vendorId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierNonConformanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ncService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierNonConformanceDto>> create(@RequestBody CreateSupplierNonConformanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ncService.create(request), "Non-conformance raised"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierNonConformanceDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateSupplierNonConformanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ncService.update(id, request), "Non-conformance updated"));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierNonConformanceDto>> respond(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                ncService.respond(id, body.get("vendorResponse")), "Response recorded"));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierNonConformanceDto>> close(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                ncService.close(id, body.get("closureNote")), "Non-conformance closed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ncService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Non-conformance deleted"));
    }
}
