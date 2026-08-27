package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePlantCategoryRequest;
import com.erp.platform.modules.agri.dto.PlantCategoryDto;
import com.erp.platform.modules.agri.service.PlantCategoryService;
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
@RequestMapping("/api/v1/agri/plant-categories")
@RequiredArgsConstructor
@Tag(name = "Plant Categories", description = "Plant category (type) management")
public class PlantCategoryController {

    private final PlantCategoryService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List plant categories")
    public ResponseEntity<ApiResponse<PageResponse<PlantCategoryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/by-family/{familyId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List categories by plant family")
    public ResponseEntity<ApiResponse<List<PlantCategoryDto>>> listByFamily(@PathVariable UUID familyId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByFamily(familyId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get plant category by ID")
    public ResponseEntity<ApiResponse<PlantCategoryDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create plant category")
    public ResponseEntity<ApiResponse<PlantCategoryDto>> create(@Valid @RequestBody CreatePlantCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Plant category created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update plant category")
    public ResponseEntity<ApiResponse<PlantCategoryDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreatePlantCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete plant category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Plant category deleted"));
    }
}
