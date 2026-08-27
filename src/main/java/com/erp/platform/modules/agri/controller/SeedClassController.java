package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSeedClassRequest;
import com.erp.platform.modules.agri.dto.SeedClassDto;
import com.erp.platform.modules.agri.service.SeedClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/seed-classes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeedClassController {

    private final SeedClassService seedClassService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeedClassDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(seedClassService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedClassDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seedClassService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeedClassDto>> create(@Valid @RequestBody CreateSeedClassRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedClassService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeedClassDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSeedClassRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seedClassService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seedClassService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
