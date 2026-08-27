package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreatePackagingDesignRequest;
import com.erp.platform.modules.agri.dto.PackagingDesignDto;
import com.erp.platform.modules.agri.service.PackagingDesignService;
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
@RequestMapping("/api/v1/agri/packaging-designs")
@RequiredArgsConstructor
@Tag(name = "Packaging Designs", description = "Packaging design management")
public class PackagingDesignController {

    private final PackagingDesignService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List packaging designs")
    public ResponseEntity<ApiResponse<PageResponse<PackagingDesignDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("productName"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get packaging design by ID")
    public ResponseEntity<ApiResponse<PackagingDesignDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create packaging design")
    public ResponseEntity<ApiResponse<PackagingDesignDto>> create(@Valid @RequestBody CreatePackagingDesignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Packaging design created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update packaging design")
    public ResponseEntity<ApiResponse<PackagingDesignDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePackagingDesignRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete packaging design")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Packaging design deleted"));
    }
}
