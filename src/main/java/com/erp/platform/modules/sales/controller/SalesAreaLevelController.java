package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesAreaLevel;
import com.erp.platform.modules.sales.repository.SalesAreaLevelRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales/area-levels")
@RequiredArgsConstructor
@Tag(name = "Sales - Area Levels", description = "Sales area level definition")
public class SalesAreaLevelController {

    private final SalesAreaLevelRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales area levels")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("name"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active sales area levels")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sales area level")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        if (str(req, "name") == null || str(req, "name").isBlank())
            throw AppException.badRequest("Name is required");
        SalesAreaLevel e = new SalesAreaLevel();
        e.setTenantId(tenantId);
        apply(e, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sales area level")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        SalesAreaLevel e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Sales area level not found: " + id));
        apply(e, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete sales area level")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        SalesAreaLevel e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Sales area level not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(SalesAreaLevel e, Map<String, Object> req) {
        if (req.containsKey("name")) e.setName(str(req, "name"));
        if (req.containsKey("description")) e.setDescription(str(req, "description"));
        if (req.containsKey("parentSalesAreaId") && str(req, "parentSalesAreaId") != null && !str(req, "parentSalesAreaId").isBlank())
            e.setParentSalesAreaId(UUID.fromString(str(req, "parentSalesAreaId")));
        if (req.containsKey("parentSalesAreaName")) e.setParentSalesAreaName(str(req, "parentSalesAreaName"));
        if (req.containsKey("roles")) e.setRoles(str(req, "roles"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(SalesAreaLevel e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName());
        m.put("description", e.getDescription() == null ? "" : e.getDescription());
        m.put("parentSalesAreaId", e.getParentSalesAreaId() == null ? "" : e.getParentSalesAreaId().toString());
        m.put("parentSalesAreaName", e.getParentSalesAreaName() == null ? "" : e.getParentSalesAreaName());
        m.put("roles", e.getRoles() == null ? "" : e.getRoles());
        m.put("active", e.isActive());
        m.put("createdAt", e.getCreatedAt() == null ? "" : e.getCreatedAt().toString());
        return m;
    }
}
