package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateWasteTypeRequest;
import com.erp.platform.modules.agri.dto.WasteTypeDto;
import com.erp.platform.modules.agri.service.WasteTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/waste-types")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WasteTypeController {

    private final WasteTypeService wasteTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WasteTypeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(wasteTypeService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WasteTypeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(wasteTypeService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WasteTypeDto>> create(@Valid @RequestBody CreateWasteTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(wasteTypeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WasteTypeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWasteTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(wasteTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        wasteTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
