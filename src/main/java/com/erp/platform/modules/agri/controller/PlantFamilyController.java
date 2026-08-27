package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePlantFamilyRequest;
import com.erp.platform.modules.agri.dto.PlantFamilyDto;
import com.erp.platform.modules.agri.service.PlantFamilyService;
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
@RequestMapping("/api/v1/agri/plant-families")
@RequiredArgsConstructor
@Tag(name = "Plant Families", description = "Plant family (group) management")
public class PlantFamilyController {

    private final PlantFamilyService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List plant families")
    public ResponseEntity<ApiResponse<PageResponse<PlantFamilyDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get plant family by ID")
    public ResponseEntity<ApiResponse<PlantFamilyDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create plant family")
    public ResponseEntity<ApiResponse<PlantFamilyDto>> create(@Valid @RequestBody CreatePlantFamilyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Plant family created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update plant family")
    public ResponseEntity<ApiResponse<PlantFamilyDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreatePlantFamilyRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete plant family")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Plant family deleted"));
    }
}
