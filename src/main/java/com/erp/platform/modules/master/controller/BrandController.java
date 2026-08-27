package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.BrandDto;
import com.erp.platform.modules.master.dto.CreateBrandRequest;
import com.erp.platform.modules.master.service.BrandService;
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
@RequestMapping("/api/v1/master/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand master data management")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List brands")
    public ResponseEntity<ApiResponse<PageResponse<BrandDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(brandService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new brand")
    public ResponseEntity<ApiResponse<BrandDto>> create(@Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(brandService.create(request), "Brand created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update brand")
    public ResponseEntity<ApiResponse<BrandDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.update(id, request), "Brand updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete brand (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        brandService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand deleted successfully"));
    }
}
