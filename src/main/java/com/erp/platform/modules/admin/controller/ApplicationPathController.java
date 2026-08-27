package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.admin.entity.ApplicationPath;
import com.erp.platform.modules.admin.repository.ApplicationPathRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/application-paths")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ApplicationPathController {

    private final ApplicationPathRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApplicationPath>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID t = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(t,
                        PageRequest.of(page, size, Sort.by("name"))))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationPath>> create(@RequestBody ApplicationPath req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.ok(ApiResponse.success(repo.save(req), "Path created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationPath>> update(@PathVariable UUID id, @RequestBody ApplicationPath req) {
        UUID t = tenantContext.current();
        ApplicationPath p = repo.findByTenantIdAndIdAndDeletedAtIsNull(t, id)
                .orElseThrow(() -> AppException.notFound("Application path not found: " + id));
        p.setName(req.getName());
        p.setPath(req.getPath());
        p.setPathType(req.getPathType());
        p.setDescription(req.getDescription());
        p.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(repo.save(p)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID t = tenantContext.current();
        ApplicationPath p = repo.findByTenantIdAndIdAndDeletedAtIsNull(t, id)
                .orElseThrow(() -> AppException.notFound("Application path not found: " + id));
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
