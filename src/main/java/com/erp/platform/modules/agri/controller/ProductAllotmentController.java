package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProductAllotmentRequest;
import com.erp.platform.modules.agri.dto.ProductAllotmentDto;
import com.erp.platform.modules.agri.service.ProductAllotmentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/product-allotments")
@RequiredArgsConstructor
@Tag(name = "Product Allotments", description = "Product allotment to dealer/distributor management")
public class ProductAllotmentController {

    private final ProductAllotmentService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List product allotments")
    public ResponseEntity<ApiResponse<PageResponse<ProductAllotmentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("allotmentDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get product allotment by ID")
    public ResponseEntity<ApiResponse<ProductAllotmentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create product allotment")
    public ResponseEntity<ApiResponse<ProductAllotmentDto>> create(@Valid @RequestBody CreateProductAllotmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Product allotment created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product allotment")
    public ResponseEntity<ApiResponse<ProductAllotmentDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateProductAllotmentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product allotment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product allotment deleted"));
    }
}
