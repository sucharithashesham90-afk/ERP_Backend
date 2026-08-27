package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.BalanceSheetConfig;
import com.erp.platform.modules.accounting.repository.BalanceSheetConfigRepository;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/balance-sheet-config")
@RequiredArgsConstructor
@Tag(name = "Accounting - Balance Sheet Config", description = "Balance sheet section configuration")
public class BalanceSheetConfigController {

    private final BalanceSheetConfigRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List balance sheet config entries")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("bsSection").and(Sort.by("displayOrder")));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create balance sheet config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        BalanceSheetConfig c = new BalanceSheetConfig();
        c.setTenantId(tenantId);
        c.setGroupName((String) req.getOrDefault("groupName", ""));
        c.setBsSection((String) req.getOrDefault("bsSection", "CURRENT_ASSETS"));
        c.setDisplayOrder(req.get("displayOrder") != null ? ((Number) req.get("displayOrder")).intValue() : 0);
        c.setShowSubTotal(req.get("showSubTotal") == null || Boolean.parseBoolean(req.get("showSubTotal").toString()));
        c.setActive(req.get("active") == null || Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(c))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update balance sheet config entry")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        BalanceSheetConfig c = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Balance sheet config not found: " + id));
        if (req.containsKey("groupName")) c.setGroupName((String) req.get("groupName"));
        if (req.containsKey("bsSection")) c.setBsSection((String) req.get("bsSection"));
        if (req.containsKey("displayOrder") && req.get("displayOrder") != null)
            c.setDisplayOrder(((Number) req.get("displayOrder")).intValue());
        if (req.containsKey("showSubTotal") && req.get("showSubTotal") != null)
            c.setShowSubTotal(Boolean.parseBoolean(req.get("showSubTotal").toString()));
        if (req.containsKey("active") && req.get("active") != null)
            c.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(c))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete balance sheet config entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        BalanceSheetConfig c = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Balance sheet config not found: " + id));
        c.setDeletedAt(LocalDateTime.now());
        repo.save(c);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(BalanceSheetConfig c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("groupName", c.getGroupName() == null ? "" : c.getGroupName());
        m.put("bsSection", c.getBsSection() == null ? "" : c.getBsSection());
        m.put("displayOrder", c.getDisplayOrder());
        m.put("showSubTotal", c.isShowSubTotal());
        m.put("active", c.isActive());
        m.put("createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
        return m;
    }
}
