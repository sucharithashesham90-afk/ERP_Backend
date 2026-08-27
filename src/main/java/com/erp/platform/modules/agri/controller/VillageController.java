package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateVillageRequest;
import com.erp.platform.modules.agri.dto.VillageDto;
import com.erp.platform.modules.agri.service.VillageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/villages")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VillageController {

    private final VillageService villageService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VillageDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(villageService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VillageDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(villageService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VillageDto>> create(@Valid @RequestBody CreateVillageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(villageService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VillageDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateVillageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(villageService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        villageService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
