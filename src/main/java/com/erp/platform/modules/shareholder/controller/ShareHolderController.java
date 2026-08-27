package com.erp.platform.modules.shareholder.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.shareholder.dto.ShareHolderDto;
import com.erp.platform.modules.shareholder.service.ShareHolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shareholder/shareholders")
@RequiredArgsConstructor
@Tag(name = "Share Holders", description = "Share Capital — Shareholder management")
public class ShareHolderController {

    private final ShareHolderService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List shareholders")
    public ResponseEntity<ApiResponse<PageResponse<ShareHolderDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("name").ascending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shareholder by ID")
    public ResponseEntity<ApiResponse<ShareHolderDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create shareholder")
    public ResponseEntity<ApiResponse<ShareHolderDto>> create(@RequestBody ShareHolderDto req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Shareholder created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update shareholder")
    public ResponseEntity<ApiResponse<ShareHolderDto>> update(@PathVariable UUID id,
            @RequestBody ShareHolderDto req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete shareholder")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Shareholder deleted"));
    }
}
