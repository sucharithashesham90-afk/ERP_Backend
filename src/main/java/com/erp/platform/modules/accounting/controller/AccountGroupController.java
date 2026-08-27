package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AccountGroup;
import com.erp.platform.modules.accounting.repository.AccountGroupRepository;
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
@RequestMapping("/api/v1/accounting/account-groups")
@RequiredArgsConstructor
@Tag(name = "Accounting - Account Groups", description = "Account group definition")
public class AccountGroupController {

    private final AccountGroupRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List account groups")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("code"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active account groups")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create account group")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String code = (String) req.get("code");
        if (repo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code))
            throw AppException.badRequest("Account group code already exists: " + code);
        AccountGroup g = new AccountGroup();
        g.setTenantId(tenantId);
        g.setCode(code);
        g.setName((String) req.get("name"));
        g.setGroupType((String) req.get("groupType"));
        g.setParentGroupCode((String) req.get("parentGroupCode"));
        g.setPrimary(Boolean.parseBoolean(req.getOrDefault("primary", "true").toString()));
        g.setEffectsGrossProfit(Boolean.parseBoolean(req.getOrDefault("effectsGrossProfit", "false").toString()));
        g.setSubAccount(Boolean.parseBoolean(req.getOrDefault("subAccount", "false").toString()));
        g.setDescription((String) req.get("description"));
        g.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(g))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update account group")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        AccountGroup g = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Account group not found: " + id));
        if (req.containsKey("name")) g.setName((String) req.get("name"));
        if (req.containsKey("groupType")) g.setGroupType((String) req.get("groupType"));
        if (req.containsKey("parentGroupCode")) g.setParentGroupCode((String) req.get("parentGroupCode"));
        if (req.containsKey("primary")) g.setPrimary(Boolean.parseBoolean(req.get("primary").toString()));
        if (req.containsKey("effectsGrossProfit")) g.setEffectsGrossProfit(Boolean.parseBoolean(req.get("effectsGrossProfit").toString()));
        if (req.containsKey("subAccount")) g.setSubAccount(Boolean.parseBoolean(req.get("subAccount").toString()));
        if (req.containsKey("description")) g.setDescription((String) req.get("description"));
        if (req.containsKey("active")) g.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(g))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete account group")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        AccountGroup g = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Account group not found: " + id));
        g.setDeletedAt(LocalDateTime.now());
        repo.save(g);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(AccountGroup g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("code", g.getCode());
        m.put("name", g.getName());
        m.put("groupType", g.getGroupType() == null ? "" : g.getGroupType());
        m.put("parentGroupCode", g.getParentGroupCode() == null ? "" : g.getParentGroupCode());
        m.put("primary", g.isPrimary());
        m.put("effectsGrossProfit", g.isEffectsGrossProfit());
        m.put("subAccount", g.isSubAccount());
        m.put("description", g.getDescription() == null ? "" : g.getDescription());
        m.put("active", g.isActive());
        m.put("createdAt", g.getCreatedAt() == null ? "" : g.getCreatedAt().toString());
        return m;
    }
}
