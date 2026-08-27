package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.CreateLocationTransferRequest;
import com.erp.platform.modules.inventory.dto.LocationTransferDto;
import com.erp.platform.modules.inventory.service.LocationTransferService;
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
@RequestMapping("/api/v1/inventory/location-transfers")
@RequiredArgsConstructor
@Tag(name = "Inventory - Location Transfers")
public class LocationTransferController {

    private final LocationTransferService transferService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LocationTransferDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID warehouseId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(transferService.list(warehouseId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationTransferDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationTransferDto>> create(@RequestBody CreateLocationTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transferService.create(request), "Location transfer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationTransferDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateLocationTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(transferService.update(id, request), "Location transfer updated"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationTransferDto>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transferService.complete(id), "Transfer completed"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationTransferDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transferService.cancel(id), "Transfer cancelled"));
    }
}
