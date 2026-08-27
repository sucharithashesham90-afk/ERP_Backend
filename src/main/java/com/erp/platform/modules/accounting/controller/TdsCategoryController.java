package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.TdsCategory;
import com.erp.platform.modules.accounting.repository.TdsCategoryRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/tds-categories")
@RequiredArgsConstructor
@Tag(name = "Accounting - TDS Categories", description = "TDS category and rate definition")
public class TdsCategoryController {

    private final TdsCategoryRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List TDS categories")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active TDS categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId).stream()
                        .map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create TDS category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> request) {
        UUID tenantId = tenantContext.current();
        TdsCategory tds = new TdsCategory();
        tds.setTenantId(tenantId);
        tds.setName((String) request.get("name"));
        tds.setRate(new BigDecimal(request.getOrDefault("rate", "0").toString()));
        tds.setSurchargeRate(new BigDecimal(request.getOrDefault("surchargeRate", "0").toString()));
        tds.setEducationCessRate(new BigDecimal(request.getOrDefault("educationCessRate", "0").toString()));
        tds.setCategoryCode((String) request.get("categoryCode"));
        tds.setThresholdAmount(new BigDecimal(request.getOrDefault("thresholdAmount", "0").toString()));
        tds.setSectionCode((String) request.get("sectionCode"));
        tds.setDescription((String) request.get("description"));
        tds.setActive(Boolean.parseBoolean(request.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(tds))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update TDS category")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> request) {
        UUID tenantId = tenantContext.current();
        TdsCategory tds = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("TDS category not found: " + id));
        if (request.containsKey("name")) tds.setName((String) request.get("name"));
        if (request.containsKey("rate")) tds.setRate(new BigDecimal(request.get("rate").toString()));
        if (request.containsKey("surchargeRate")) tds.setSurchargeRate(new BigDecimal(request.get("surchargeRate").toString()));
        if (request.containsKey("educationCessRate")) tds.setEducationCessRate(new BigDecimal(request.get("educationCessRate").toString()));
        if (request.containsKey("categoryCode")) tds.setCategoryCode((String) request.get("categoryCode"));
        if (request.containsKey("thresholdAmount")) {
            tds.setThresholdAmount(new BigDecimal(request.get("thresholdAmount").toString()));
        }
        if (request.containsKey("sectionCode")) tds.setSectionCode((String) request.get("sectionCode"));
        if (request.containsKey("description")) tds.setDescription((String) request.get("description"));
        if (request.containsKey("active")) tds.setActive(Boolean.parseBoolean(request.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(tds))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete TDS category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        TdsCategory tds = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("TDS category not found: " + id));
        tds.setDeletedAt(LocalDateTime.now());
        repo.save(tds);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(TdsCategory t) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("categoryCode", t.getCategoryCode() == null ? "" : t.getCategoryCode());
        m.put("thresholdAmount", t.getThresholdAmount() == null ? BigDecimal.ZERO : t.getThresholdAmount());
        m.put("rate", t.getRate());
        m.put("surchargeRate", t.getSurchargeRate());
        m.put("educationCessRate", t.getEducationCessRate());
        m.put("sectionCode", t.getSectionCode() == null ? "" : t.getSectionCode());
        m.put("description", t.getDescription() == null ? "" : t.getDescription());
        m.put("active", t.isActive());
        m.put("createdAt", t.getCreatedAt() == null ? "" : t.getCreatedAt().toString());
        return m;
    }
}
