package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProductionPlanRequest;
import com.erp.platform.modules.agri.dto.ProductionPlanDto;
import com.erp.platform.modules.agri.service.AgriProductionPlanService;
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
@RequestMapping("/api/v1/agri/production-plans")
@RequiredArgsConstructor
@Tag(name = "Agri Production Plans", description = "Agricultural production planning")
public class AgriProductionPlanController {

    private final AgriProductionPlanService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List agri production plans")
    public ResponseEntity<ApiResponse<PageResponse<ProductionPlanDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("planNumber")))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get agri production plan by ID")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create agri production plan")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> create(@Valid @RequestBody CreateProductionPlanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Production plan created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update agri production plan")
    public ResponseEntity<ApiResponse<ProductionPlanDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateProductionPlanRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete agri production plan")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Production plan deleted"));
    }
}
