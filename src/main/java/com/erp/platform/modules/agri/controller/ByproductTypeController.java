package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.ByproductTypeDto;
import com.erp.platform.modules.agri.dto.CreateByproductTypeRequest;
import com.erp.platform.modules.agri.service.ByproductTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/byproduct-types")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ByproductTypeController {

    private final ByproductTypeService byproductTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ByproductTypeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(byproductTypeService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ByproductTypeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(byproductTypeService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ByproductTypeDto>> create(@Valid @RequestBody CreateByproductTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(byproductTypeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ByproductTypeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateByproductTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(byproductTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        byproductTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
