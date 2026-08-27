package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.master.dto.RoleHierarchyDto;
import com.erp.platform.modules.master.dto.RoleHierarchyRequest;
import com.erp.platform.modules.master.service.RoleHierarchyService;
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
@RequestMapping("/api/v1/master/role-hierarchy")
@RequiredArgsConstructor
@Tag(name = "Role Hierarchy", description = "Company role hierarchy configuration")
public class RoleHierarchyController {

    private final RoleHierarchyService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List role hierarchy (ordered by level)")
    public ResponseEntity<ApiResponse<List<RoleHierarchyDto>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get role hierarchy entry by ID")
    public ResponseEntity<ApiResponse<RoleHierarchyDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add role to hierarchy")
    public ResponseEntity<ApiResponse<RoleHierarchyDto>> create(@Valid @RequestBody RoleHierarchyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Role added to hierarchy"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update role hierarchy entry")
    public ResponseEntity<ApiResponse<RoleHierarchyDto>> update(
            @PathVariable UUID id, @Valid @RequestBody RoleHierarchyRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove role from hierarchy")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role removed from hierarchy"));
    }
}
