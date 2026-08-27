package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateProductCategoryRequest;
import com.erp.platform.modules.master.dto.ProductCategoryDto;
import com.erp.platform.modules.master.service.ProductCategoryService;
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
@RequestMapping("/api/v1/master/product-categories")
@RequiredArgsConstructor
@Tag(name = "Product Categories", description = "Product category management")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List product categories")
    public ResponseEntity<ApiResponse<PageResponse<ProductCategoryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(categoryService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get product category by ID")
    public ResponseEntity<ApiResponse<ProductCategoryDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a product category")
    public ResponseEntity<ApiResponse<ProductCategoryDto>> create(@Valid @RequestBody CreateProductCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(categoryService.create(request), "Category created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product category")
    public ResponseEntity<ApiResponse<ProductCategoryDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProductCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product category (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
