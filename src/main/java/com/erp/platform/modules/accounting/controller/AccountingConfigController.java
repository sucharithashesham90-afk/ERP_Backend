package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AccountingConfig;
import com.erp.platform.modules.accounting.repository.AccountingConfigRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/config")
@RequiredArgsConstructor
@Tag(name = "Accounting - Configuration", description = "Accounting module configuration and feature flags")
public class AccountingConfigController {

    private final AccountingConfigRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List accounting config entries")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("category", "configKey"))).map(this::toMap))));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all accounting config as flat list")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAll() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String key = (String) req.get("configKey");
        if (repo.existsByTenantIdAndConfigKeyAndDeletedAtIsNull(tenantId, key))
            throw AppException.badRequest("Config key already exists: " + key);
        AccountingConfig cfg = new AccountingConfig();
        cfg.setTenantId(tenantId);
        cfg.setConfigKey(key);
        cfg.setConfigValue((String) req.get("configValue"));
        cfg.setCategory((String) req.get("category"));
        cfg.setDescription((String) req.get("description"));
        cfg.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(cfg))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        AccountingConfig cfg = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Config not found: " + id));
        if (req.containsKey("configValue")) cfg.setConfigValue((String) req.get("configValue"));
        if (req.containsKey("category")) cfg.setCategory((String) req.get("category"));
        if (req.containsKey("description")) cfg.setDescription((String) req.get("description"));
        if (req.containsKey("active")) cfg.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(cfg))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete config entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        AccountingConfig cfg = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Config not found: " + id));
        cfg.setDeletedAt(LocalDateTime.now());
        repo.save(cfg);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(AccountingConfig c) {
        return Map.of(
                "id", c.getId(),
                "configKey", c.getConfigKey(),
                "configValue", c.getConfigValue() == null ? "" : c.getConfigValue(),
                "category", c.getCategory() == null ? "" : c.getCategory(),
                "description", c.getDescription() == null ? "" : c.getDescription(),
                "active", c.isActive(),
                "createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString()
        );
    }
}
