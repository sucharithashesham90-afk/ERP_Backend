package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.DiscountDefinition;
import com.erp.platform.modules.accounting.repository.DiscountDefinitionRepository;
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
@RequestMapping("/api/v1/accounting/discount-definitions")
@RequiredArgsConstructor
@Tag(name = "Accounting - Discount Definitions", description = "Named discount-to-ledger configurations")
public class DiscountDefinitionController {

    private final DiscountDefinitionRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List discount definitions")
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
    @Operation(summary = "List active discount definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create discount definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String name = str(req, "name");
        if (name == null || name.isBlank())
            throw AppException.badRequest("Name is required");
        DiscountDefinition d = new DiscountDefinition();
        d.setTenantId(tenantId);
        apply(d, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(d))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update discount definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        DiscountDefinition d = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Discount definition not found: " + id));
        apply(d, req);
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(d))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete discount definition")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        DiscountDefinition d = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Discount definition not found: " + id));
        d.setDeletedAt(LocalDateTime.now());
        repo.save(d);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void apply(DiscountDefinition d, Map<String, Object> req) {
        if (req.containsKey("name")) d.setName(str(req, "name"));
        if (req.containsKey("ledgerId") && str(req, "ledgerId") != null && !str(req, "ledgerId").isBlank())
            d.setLedgerId(UUID.fromString(str(req, "ledgerId")));
        if (req.containsKey("ledgerName")) d.setLedgerName(str(req, "ledgerName"));
        if (req.containsKey("ledgerCode")) d.setLedgerCode(str(req, "ledgerCode"));
        if (req.containsKey("active"))     d.setActive(Boolean.parseBoolean(req.get("active").toString()));
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(DiscountDefinition d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("name", d.getName());
        m.put("ledgerId", d.getLedgerId() == null ? "" : d.getLedgerId().toString());
        m.put("ledgerName", d.getLedgerName() == null ? "" : d.getLedgerName());
        m.put("ledgerCode", d.getLedgerCode() == null ? "" : d.getLedgerCode());
        m.put("active", d.isActive());
        m.put("createdAt", d.getCreatedAt() == null ? "" : d.getCreatedAt().toString());
        return m;
    }
}
