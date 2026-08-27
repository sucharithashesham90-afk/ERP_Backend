package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreatePhysicalCountRequest;
import com.erp.platform.modules.inventory.dto.PhysicalCountDto;
import com.erp.platform.modules.inventory.service.PhysicalCountService;
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
@RequestMapping("/api/v1/inventory/physical-counts")
@RequiredArgsConstructor
@Tag(name = "Inventory - Physical Counts", description = "Physical stock count management")
public class PhysicalCountController {

    private final PhysicalCountService physicalCountService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List physical counts")
    public ResponseEntity<ApiResponse<PageResponse<PhysicalCountDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(physicalCountService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get physical count by ID")
    public ResponseEntity<ApiResponse<PhysicalCountDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(physicalCountService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create physical count with item lines")
    public ResponseEntity<ApiResponse<PhysicalCountDto>> create(@RequestBody CreatePhysicalCountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(physicalCountService.create(request)));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit physical count — adjusts stock balances to counted quantities")
    public ResponseEntity<ApiResponse<PhysicalCountDto>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(physicalCountService.submit(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete physical count (DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        physicalCountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
