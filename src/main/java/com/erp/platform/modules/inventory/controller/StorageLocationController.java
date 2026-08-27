package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateStorageLocationRequest;
import com.erp.platform.modules.inventory.dto.LocationStockDto;
import com.erp.platform.modules.inventory.dto.StorageLocationDto;
import com.erp.platform.modules.inventory.service.StorageLocationService;
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
@RequestMapping("/api/v1/inventory/storage-locations")
@RequiredArgsConstructor
@Tag(name = "Inventory - Storage Locations")
public class StorageLocationController {

    private final StorageLocationService locationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<StorageLocationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID warehouseId) {
        var pageable = PageRequest.of(page, size, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(locationService.list(warehouseId, pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<java.util.List<StorageLocationDto>>> listActive() {
        var pageable = PageRequest.of(0, 1000, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(locationService.list(null, pageable).getContent()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<StorageLocationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getById(id)));
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LocationStockDto>>> getStock(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(ApiResponse.success(locationService.getLocationStock(id, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<StorageLocationDto>> create(@Valid @RequestBody CreateStorageLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(locationService.create(request), "Storage location created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<StorageLocationDto>> update(
            @PathVariable UUID id, @RequestBody CreateStorageLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(locationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Storage location deleted"));
    }
}
