package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.SalesConfig;
import com.erp.platform.modules.sales.repository.SalesConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/sales/config")
@RequiredArgsConstructor
@Tag(name = "Sales - Configuration", description = "Sales module config & application feature toggles")
public class SalesConfigController {

    private final SalesConfigRepository repo;
    private final TenantContext tenantContext;

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all sales config entries")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> all() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a sales config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String key = str(req, "configKey");
        if (key == null || key.isBlank()) throw AppException.badRequest("configKey is required");
        if (repo.existsByTenantIdAndConfigKeyAndDeletedAtIsNull(tenantId, key))
            throw AppException.badRequest("Config key already exists: " + key);
        SalesConfig c = new SalesConfig();
        c.setTenantId(tenantId);
        c.setConfigKey(key);
        c.setConfigValue(str(req, "configValue"));
        c.setCategory(req.getOrDefault("category", "FEATURE").toString());
        c.setDescription(str(req, "description"));
        c.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(c))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a sales config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        SalesConfig c = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Config not found: " + id));
        if (req.containsKey("configValue")) c.setConfigValue(str(req, "configValue"));
        if (req.containsKey("category")) c.setCategory(str(req, "category"));
        if (req.containsKey("description")) c.setDescription(str(req, "description"));
        if (req.containsKey("active")) c.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(c))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a sales config entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        SalesConfig c = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Config not found: " + id));
        c.setDeletedAt(LocalDateTime.now());
        repo.save(c);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(SalesConfig c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("configKey", c.getConfigKey());
        m.put("configValue", c.getConfigValue() == null ? "" : c.getConfigValue());
        m.put("category", c.getCategory() == null ? "" : c.getCategory());
        m.put("description", c.getDescription() == null ? "" : c.getDescription());
        m.put("active", c.isActive());
        return m;
    }
}
