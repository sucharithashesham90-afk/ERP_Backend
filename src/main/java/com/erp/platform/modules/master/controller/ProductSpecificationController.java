package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.master.entity.ProductSpecification;
import com.erp.platform.modules.master.service.ProductSpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Master - Product Specifications", description = "Product specification management")
public class ProductSpecificationController {

    private final ProductSpecificationService service;

    @GetMapping("/api/v1/master/products/{productId}/specifications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get specifications for product")
    public ResponseEntity<ApiResponse<List<ProductSpecification>>> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByProduct(productId)));
    }

    @PostMapping("/api/v1/master/products/{productId}/specifications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create product specification")
    public ResponseEntity<ApiResponse<ProductSpecification>> create(
            @PathVariable UUID productId,
            @RequestBody ProductSpecification req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(productId, req), "Created"));
    }

    @PutMapping("/api/v1/master/specifications/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update product specification")
    public ResponseEntity<ApiResponse<ProductSpecification>> update(
            @PathVariable UUID id,
            @RequestBody ProductSpecification req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/api/v1/master/specifications/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete product specification")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PostMapping("/api/v1/master/products/{productId}/specifications/bulk")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk save specifications for product")
    public ResponseEntity<ApiResponse<List<ProductSpecification>>> bulkSave(
            @PathVariable UUID productId,
            @RequestBody List<ProductSpecification> specs) {
        return ResponseEntity.ok(ApiResponse.success(service.bulkSave(productId, specs), "Saved"));
    }
}
