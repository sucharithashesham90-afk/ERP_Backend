package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateProductRequest;
import com.erp.platform.modules.master.dto.ProductDto;
import com.erp.platform.modules.master.dto.UpdateProductRequest;
import com.erp.platform.modules.master.service.ProductService;
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
@RequestMapping("/api/v1/master/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product master data management")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List products with pagination, search, and category filter (MATERIAL or PRODUCT)")
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(productService.list(search, category, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDto>> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productService.create(request), "Product created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}
