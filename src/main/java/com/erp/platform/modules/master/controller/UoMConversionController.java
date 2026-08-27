package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateUoMConversionRequest;
import com.erp.platform.modules.master.dto.UoMConversionDto;
import com.erp.platform.modules.master.service.UoMConversionService;
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
@RequestMapping("/api/v1/master/uom-conversions")
@RequiredArgsConstructor
@Tag(name = "UoM Conversions", description = "Unit of Measure conversion management")
public class UoMConversionController {

    private final UoMConversionService conversionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List UoM conversions")
    public ResponseEntity<ApiResponse<PageResponse<UoMConversionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID fromUomId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(conversionService.list(fromUomId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get UoM conversion by ID")
    public ResponseEntity<ApiResponse<UoMConversionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(conversionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new UoM conversion")
    public ResponseEntity<ApiResponse<UoMConversionDto>> create(@Valid @RequestBody CreateUoMConversionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(conversionService.create(request), "UoM conversion created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update UoM conversion")
    public ResponseEntity<ApiResponse<UoMConversionDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateUoMConversionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(conversionService.update(id, request), "UoM conversion updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete UoM conversion (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        conversionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "UoM conversion deleted successfully"));
    }
}
