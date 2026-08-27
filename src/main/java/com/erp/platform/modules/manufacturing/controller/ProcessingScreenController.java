package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.MfgProcessingScreen;
import com.erp.platform.modules.manufacturing.repository.MfgProcessingScreenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController("mfgProcessingScreenController")
@RequestMapping("/api/v1/manufacturing/processing-screens")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Processing Screens", description = "Processing screen/sieve management")
public class ProcessingScreenController {

    private final MfgProcessingScreenRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List processing screens")
    public ResponseEntity<ApiResponse<PageResponse<MfgProcessingScreen>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(
                        tenantContext.current(), PageRequest.of(page, size, Sort.by("name"))))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active processing screens for dropdown")
    public ResponseEntity<ApiResponse<List<MfgProcessingScreen>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create processing screen")
    public ResponseEntity<ApiResponse<MfgProcessingScreen>> create(@RequestBody MfgProcessingScreen req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(repo.save(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update processing screen")
    public ResponseEntity<ApiResponse<MfgProcessingScreen>> update(@PathVariable UUID id, @RequestBody MfgProcessingScreen req) {
        MfgProcessingScreen e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Processing screen not found: " + id));
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete processing screen")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        MfgProcessingScreen e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Processing screen not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

