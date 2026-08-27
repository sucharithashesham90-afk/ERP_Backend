package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.CreateProductionAppFeatureRequest;
import com.erp.platform.modules.admin.dto.ProductionAppFeatureDto;
import com.erp.platform.modules.admin.service.ProductionAppFeatureService;
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
@RequestMapping("/api/v1/admin/production-app-features")
@RequiredArgsConstructor
@Tag(name = "Production App Features", description = "Production app feature flag management")
public class ProductionAppFeatureController {

    private final ProductionAppFeatureService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List production app features")
    public ResponseEntity<ApiResponse<PageResponse<ProductionAppFeatureDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("featureName"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get production app feature by ID")
    public ResponseEntity<ApiResponse<ProductionAppFeatureDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create production app feature")
    public ResponseEntity<ApiResponse<ProductionAppFeatureDto>> create(@Valid @RequestBody CreateProductionAppFeatureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Production app feature created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update production app feature")
    public ResponseEntity<ApiResponse<ProductionAppFeatureDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductionAppFeatureRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete production app feature")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Production app feature deleted"));
    }
}
