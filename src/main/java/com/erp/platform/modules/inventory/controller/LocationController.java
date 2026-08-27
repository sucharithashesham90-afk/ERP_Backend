package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateLocationRequest;
import com.erp.platform.modules.inventory.dto.LocationDto;
import com.erp.platform.modules.inventory.service.LocationService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Warehouse location master management")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/api/v1/inventory/locations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all locations (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<LocationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(locationService.list(pageable)));
    }

    @GetMapping("/api/v1/inventory/locations/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active locations for dropdowns")
    public ResponseEntity<ApiResponse<List<LocationDto>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(locationService.listActive()));
    }

    @GetMapping("/api/v1/inventory/locations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get location by ID")
    public ResponseEntity<ApiResponse<LocationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(locationService.getById(id)));
    }

    @PostMapping("/api/v1/inventory/locations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create location")
    public ResponseEntity<ApiResponse<LocationDto>> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(locationService.create(request), "Location created successfully"));
    }

    @PutMapping("/api/v1/inventory/locations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update location")
    public ResponseEntity<ApiResponse<LocationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(locationService.update(id, request)));
    }

    @DeleteMapping("/api/v1/inventory/locations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete location (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Location deleted successfully"));
    }
}
