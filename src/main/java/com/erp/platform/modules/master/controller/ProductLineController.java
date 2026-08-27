package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateProductLineRequest;
import com.erp.platform.modules.master.dto.ProductLineDto;
import com.erp.platform.modules.master.service.ProductLineService;
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
@RequestMapping("/api/v1/master/product-lines")
@RequiredArgsConstructor
@Tag(name = "Product Lines", description = "Product line master data management")
public class ProductLineController {

    private final ProductLineService productLineService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List product lines")
    public ResponseEntity<ApiResponse<PageResponse<ProductLineDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(productLineService.list(brandId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get product line by ID")
    public ResponseEntity<ApiResponse<ProductLineDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new product line")
    public ResponseEntity<ApiResponse<ProductLineDto>> create(@Valid @RequestBody CreateProductLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productLineService.create(request), "Product line created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product line")
    public ResponseEntity<ApiResponse<ProductLineDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductLineRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productLineService.update(id, request), "Product line updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product line (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productLineService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product line deleted successfully"));
    }
}
