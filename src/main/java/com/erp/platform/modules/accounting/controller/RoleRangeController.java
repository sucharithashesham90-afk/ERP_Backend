package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.RoleRange;
import com.erp.platform.modules.accounting.repository.RoleRangeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/role-ranges")
@RequiredArgsConstructor
@Tag(name = "Accounting - Role Ranges", description = "Per-role transaction posting limits (INR)")
public class RoleRangeController {

    private final RoleRangeRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List role ranges")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("roleName"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active role ranges")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create role range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String roleName = str(req, "roleName");
        if (roleName == null || roleName.isBlank())
            throw AppException.badRequest("Role is required");
        if (repo.findByTenantIdAndRoleNameAndDeletedAtIsNull(tenantId, roleName).isPresent())
            throw AppException.badRequest("Role range already exists for role: " + roleName);
        RoleRange r = new RoleRange();
        r.setTenantId(tenantId);
        apply(r, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(r))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update role range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        RoleRange r = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Role range not found: " + id));
        apply(r, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(r))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete role range")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        RoleRange r = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Role range not found: " + id));
        r.setDeletedAt(LocalDateTime.now());
        repo.save(r);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(RoleRange r, Map<String, Object> req) {
        if (req.containsKey("roleId") && str(req, "roleId") != null && !str(req, "roleId").isBlank())
            r.setRoleId(UUID.fromString(str(req, "roleId")));
        if (req.containsKey("roleName")) r.setRoleName(str(req, "roleName"));
        if (req.containsKey("rangeInr")) r.setRangeInr(new BigDecimal(req.getOrDefault("rangeInr", "0").toString()));
        if (req.containsKey("active"))   r.setActive(Boolean.parseBoolean(req.get("active").toString()));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(RoleRange r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("roleId", r.getRoleId() == null ? "" : r.getRoleId().toString());
        m.put("roleName", r.getRoleName());
        m.put("rangeInr", r.getRangeInr() == null ? BigDecimal.ZERO : r.getRangeInr());
        m.put("active", r.isActive());
        m.put("createdAt", r.getCreatedAt() == null ? "" : r.getCreatedAt().toString());
        return m;
    }
}
