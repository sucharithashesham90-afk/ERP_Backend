package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.entity.SalesRepresentative;
import com.erp.platform.modules.sales.entity.SalesTerritory;
import com.erp.platform.modules.sales.service.SalesTerritoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/sales/territories")
@RequiredArgsConstructor
@Tag(name = "Sales - Territories", description = "Sales territory management")
public class SalesTerritoryController {

    private final SalesTerritoryService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales territories")
    public ResponseEntity<ApiResponse<PageResponse<SalesTerritory>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get territory by ID")
    public ResponseEntity<ApiResponse<SalesTerritory>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create territory")
    public ResponseEntity<ApiResponse<SalesTerritory>> create(@RequestBody SalesTerritory req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update territory")
    public ResponseEntity<ApiResponse<SalesTerritory>> update(@PathVariable UUID id, @RequestBody SalesTerritory req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete territory")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @GetMapping("/{id}/reps")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get reps in territory")
    public ResponseEntity<ApiResponse<List<SalesRepresentative>>> getReps(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getRepsForTerritory(id)));
    }
}
