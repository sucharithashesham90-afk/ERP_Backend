package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.MaterialItemDto;
import com.erp.platform.modules.master.dto.MaterialItemRequest;
import com.erp.platform.modules.master.service.MaterialItemService;
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
@RequestMapping("/api/v1/master/material-items")
@RequiredArgsConstructor
@Tag(name = "Material Items", description = "Material item / grade / variant master data")
public class MaterialItemController {

    private final MaterialItemService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List material items — all, or filtered by materialId")
    public ResponseEntity<?> list(
            @RequestParam(required = false) UUID materialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        if (materialId != null) {
            List<MaterialItemDto> items = service.listByMaterial(materialId);
            return ResponseEntity.ok(ApiResponse.success(items));
        }
        if (size >= 200) {
            return ResponseEntity.ok(ApiResponse.success(service.listAll()));
        }
        PageResponse<MaterialItemDto> result = service.list(
                PageRequest.of(page, size, Sort.by("materialName", "name")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MaterialItemDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create material item")
    public ResponseEntity<ApiResponse<MaterialItemDto>> create(@RequestBody MaterialItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Material item created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update material item")
    public ResponseEntity<ApiResponse<MaterialItemDto>> update(
            @PathVariable UUID id, @RequestBody MaterialItemRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete material item (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material item deleted"));
    }
}
