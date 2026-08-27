package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateFarmerRequest;
import com.erp.platform.modules.agri.dto.FarmerDto;
import com.erp.platform.modules.agri.service.FarmerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/farmers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FarmerController {

    private final FarmerService farmerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FarmerDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(farmerService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(farmerService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FarmerDto>> create(@Valid @RequestBody CreateFarmerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(farmerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmerDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFarmerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(farmerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        farmerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
