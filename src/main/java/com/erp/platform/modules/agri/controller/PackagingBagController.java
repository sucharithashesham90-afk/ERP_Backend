package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePackagingBagRequest;
import com.erp.platform.modules.agri.dto.PackagingBagDto;
import com.erp.platform.modules.agri.service.PackagingBagService;
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
@RequestMapping("/api/v1/agri/packaging-bags")
@RequiredArgsConstructor
@Tag(name = "Packaging Bags", description = "Packaging bag management")
public class PackagingBagController {

    private final PackagingBagService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List packaging bags")
    public ResponseEntity<ApiResponse<PageResponse<PackagingBagDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get packaging bag by ID")
    public ResponseEntity<ApiResponse<PackagingBagDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create packaging bag")
    public ResponseEntity<ApiResponse<PackagingBagDto>> create(@Valid @RequestBody CreatePackagingBagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Packaging bag created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update packaging bag")
    public ResponseEntity<ApiResponse<PackagingBagDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePackagingBagRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete packaging bag")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Packaging bag deleted"));
    }
}
