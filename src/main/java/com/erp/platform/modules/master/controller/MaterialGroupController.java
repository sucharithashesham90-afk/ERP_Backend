package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.master.dto.MaterialGroupDto;
import com.erp.platform.modules.master.dto.MaterialGroupRequest;
import com.erp.platform.modules.master.service.MaterialGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master/material-groups")
@RequiredArgsConstructor
@Tag(name = "Material Groups", description = "Material group master data")
public class MaterialGroupController {

    private final MaterialGroupService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all material groups")
    public ResponseEntity<ApiResponse<List<MaterialGroupDto>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get material group by ID")
    public ResponseEntity<ApiResponse<MaterialGroupDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create material group")
    public ResponseEntity<ApiResponse<MaterialGroupDto>> create(@Valid @RequestBody MaterialGroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Material group created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update material group")
    public ResponseEntity<ApiResponse<MaterialGroupDto>> update(
            @PathVariable UUID id, @Valid @RequestBody MaterialGroupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete material group (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material group deleted"));
    }
}
