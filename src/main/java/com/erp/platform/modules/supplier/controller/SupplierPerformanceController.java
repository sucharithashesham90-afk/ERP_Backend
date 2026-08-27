package com.erp.platform.modules.supplier.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.supplier.dto.CreateSupplierPerformanceRequest;
import com.erp.platform.modules.supplier.dto.SupplierPerformanceDto;
import com.erp.platform.modules.supplier.service.SupplierPerformanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/supplier/performance")
@RequiredArgsConstructor
@Tag(name = "Supplier - Performance")
public class SupplierPerformanceController {

    private final SupplierPerformanceService perfService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<SupplierPerformanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID vendorId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(perfService.list(vendorId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierPerformanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(perfService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierPerformanceDto>> create(@RequestBody CreateSupplierPerformanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(perfService.create(request), "Performance record created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SupplierPerformanceDto>> update(
            @PathVariable UUID id, @RequestBody CreateSupplierPerformanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(perfService.update(id, request)));
    }
}
