package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.dto.UpdateVendorRequest;
import com.erp.platform.modules.master.dto.VendorDto;
import com.erp.platform.modules.master.service.VendorService;
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
@RequestMapping("/api/v1/master/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendors", description = "Vendor master data management")
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List vendors with pagination and search")
    public ResponseEntity<ApiResponse<PageResponse<VendorDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(vendorService.list(search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<ApiResponse<VendorDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new vendor")
    public ResponseEntity<ApiResponse<VendorDto>> create(@Valid @RequestBody CreateVendorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vendorService.create(request), "Vendor created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update vendor")
    public ResponseEntity<ApiResponse<VendorDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVendorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vendorService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete vendor (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vendorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Vendor deleted successfully"));
    }
}
