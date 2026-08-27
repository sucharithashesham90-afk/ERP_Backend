package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSalesSchemeRequest;
import com.erp.platform.modules.agri.dto.SalesSchemeDto;
import com.erp.platform.modules.agri.service.SalesSchemeService;
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
@RequestMapping("/api/v1/agri/sales-schemes")
@RequiredArgsConstructor
@Tag(name = "Sales Schemes", description = "Pricing and discount scheme management")
public class SalesSchemeController {

    private final SalesSchemeService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales schemes")
    public ResponseEntity<ApiResponse<PageResponse<SalesSchemeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("schemeName")))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sales scheme by ID")
    public ResponseEntity<ApiResponse<SalesSchemeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sales scheme")
    public ResponseEntity<ApiResponse<SalesSchemeDto>> create(@Valid @RequestBody CreateSalesSchemeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Sales scheme created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales scheme")
    public ResponseEntity<ApiResponse<SalesSchemeDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateSalesSchemeRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete sales scheme")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sales scheme deleted"));
    }
}
