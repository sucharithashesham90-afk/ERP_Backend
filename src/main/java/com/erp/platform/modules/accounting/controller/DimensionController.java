package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.*;
import com.erp.platform.modules.accounting.service.DimensionService;
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
@RequestMapping("/api/v1/accounting/dimensions")
@RequiredArgsConstructor
@Tag(name = "Accounting - Dimensions", description = "Multi-dimensional accounting configuration")
public class DimensionController {

    private final DimensionService dimensionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all dimensions")
    public ResponseEntity<ApiResponse<PageResponse<DimensionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(dimensionService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get dimension by ID with its values")
    public ResponseEntity<ApiResponse<DimensionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(dimensionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new dimension")
    public ResponseEntity<ApiResponse<DimensionDto>> create(@Valid @RequestBody CreateDimensionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dimensionService.create(request), "Dimension created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a dimension")
    public ResponseEntity<ApiResponse<DimensionDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDimensionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dimensionService.update(id, request), "Dimension updated successfully"));
    }

    @PostMapping("/{id}/values")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a value to a dimension")
    public ResponseEntity<ApiResponse<DimensionValueDto>> addValue(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDimensionValueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dimensionService.addValue(id, request), "Dimension value added successfully"));
    }

    @GetMapping("/{id}/values")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List values for a dimension")
    public ResponseEntity<ApiResponse<PageResponse<DimensionValueDto>>> listValues(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(dimensionService.listValues(id, pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a dimension (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        dimensionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dimension deleted successfully"));
    }
}
