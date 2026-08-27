package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePlantVariantRequest;
import com.erp.platform.modules.agri.dto.PlantVariantDto;
import com.erp.platform.modules.agri.service.PlantVariantService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/plant-variants")
@RequiredArgsConstructor
@Tag(name = "Plant Variants", description = "Plant variant/cultivar management")
public class PlantVariantController {

    private final PlantVariantService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List plant variants")
    public ResponseEntity<ApiResponse<PageResponse<PlantVariantDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) UUID cropId,
            @RequestParam(required = false) UUID cropGroupId) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.list(cropId, cropGroupId, pageable)));
    }

    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List variants by plant category")
    public ResponseEntity<ApiResponse<List<PlantVariantDto>>> listByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByCategory(categoryId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get plant variant by ID")
    public ResponseEntity<ApiResponse<PlantVariantDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create plant variant")
    public ResponseEntity<ApiResponse<PlantVariantDto>> create(@Valid @RequestBody CreatePlantVariantRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Plant variant created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update plant variant")
    public ResponseEntity<ApiResponse<PlantVariantDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreatePlantVariantRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete plant variant")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Plant variant deleted"));
    }
}
