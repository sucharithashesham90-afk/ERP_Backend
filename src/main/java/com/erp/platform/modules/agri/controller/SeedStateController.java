package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedStateRequest;
import com.erp.platform.modules.agri.dto.SeedStateDto;
import com.erp.platform.modules.agri.service.SeedStateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/seed-states")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeedStateController {

    private final SeedStateService seedStateService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeedStateDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(seedStateService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedStateDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seedStateService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeedStateDto>> create(@Valid @RequestBody CreateSeedStateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedStateService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedStateDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSeedStateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedStateService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seedStateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
