package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.admin.entity.AdminLocation;
import com.erp.platform.modules.admin.repository.AdminLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/locations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AdminLocationController {

    private final AdminLocationRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminLocation>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID t = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(t,
                        PageRequest.of(page, size, Sort.by("name"))))));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AdminLocation>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), true)));
    }

    /** All active locations of a given type — feeds Production Area / Processing Plant dropdowns. */
    @GetMapping("/active/by-type/{type}")
    public ResponseEntity<ApiResponse<List<AdminLocation>>> listActiveByType(@PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndLocationTypeAndActiveAndDeletedAtIsNullOrderByNameAsc(
                        tenantContext.current(), type.toUpperCase(), true)));
    }

    /**
     * Processing Plants that belong to a specific Production Area.
     * Used by cascading dropdowns (e.g. Variety form: pick PA → load its plants).
     */
    @GetMapping("/active/by-parent/{parentId}")
    public ResponseEntity<ApiResponse<List<AdminLocation>>> listByParent(@PathVariable UUID parentId) {
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndParentIdAndActiveAndDeletedAtIsNullOrderByNameAsc(
                        tenantContext.current(), parentId, true)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminLocation>> create(@RequestBody AdminLocation req) {
        UUID t = tenantContext.current();
        req.setTenantId(t);
        resolveParentName(req, t);
        return ResponseEntity.ok(ApiResponse.success(repo.save(req), "Location created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminLocation>> update(@PathVariable UUID id, @RequestBody AdminLocation req) {
        UUID t = tenantContext.current();
        AdminLocation loc = repo.findByTenantIdAndIdAndDeletedAtIsNull(t, id)
                .orElseThrow(() -> AppException.notFound("Location not found: " + id));
        loc.setName(req.getName());
        loc.setCode(req.getCode());
        loc.setLocationType(req.getLocationType());
        loc.setDescription(req.getDescription());
        loc.setActive(req.isActive());
        loc.setParentId(req.getParentId());
        resolveParentName(loc, t);
        return ResponseEntity.ok(ApiResponse.success(repo.save(loc)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID t = tenantContext.current();
        AdminLocation loc = repo.findByTenantIdAndIdAndDeletedAtIsNull(t, id)
                .orElseThrow(() -> AppException.notFound("Location not found: " + id));
        loc.setDeletedAt(LocalDateTime.now());
        repo.save(loc);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void resolveParentName(AdminLocation loc, UUID tenantId) {
        if (loc.getParentId() != null) {
            repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, loc.getParentId())
                    .ifPresent(parent -> loc.setParentName(parent.getName()));
        } else {
            loc.setParentName(null);
        }
    }
}
