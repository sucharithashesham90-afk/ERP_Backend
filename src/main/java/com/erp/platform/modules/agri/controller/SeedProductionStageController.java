package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedProductionStageRequest;
import com.erp.platform.modules.agri.dto.SeedProductionStageDto;
import com.erp.platform.modules.agri.service.SeedProductionStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/seed-production-stages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeedProductionStageController {

    private final SeedProductionStageService seedProductionStageService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeedProductionStageDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(seedProductionStageService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedProductionStageDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seedProductionStageService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeedProductionStageDto>> create(@Valid @RequestBody CreateSeedProductionStageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedProductionStageService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedProductionStageDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSeedProductionStageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedProductionStageService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seedProductionStageService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
