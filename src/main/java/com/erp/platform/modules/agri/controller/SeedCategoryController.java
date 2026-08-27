package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedCategoryRequest;
import com.erp.platform.modules.agri.dto.SeedCategoryDto;
import com.erp.platform.modules.agri.service.SeedCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/seed-categories")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeedCategoryController {

    private final SeedCategoryService seedCategoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeedCategoryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(seedCategoryService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedCategoryDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seedCategoryService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeedCategoryDto>> create(@Valid @RequestBody CreateSeedCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedCategoryDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSeedCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedCategoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seedCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
