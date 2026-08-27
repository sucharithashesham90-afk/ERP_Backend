package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.VatDefinition;
import com.erp.platform.modules.accounting.repository.VatDefinitionRepository;
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
@RequestMapping("/api/v1/accounting/vat-definitions")
@RequiredArgsConstructor
@Tag(name = "Accounting - VAT/GST Definitions", description = "VAT and GST rate definition")
public class VatDefinitionController {

    private final VatDefinitionRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List VAT definitions")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("code"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active VAT definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create VAT definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String code = (String) req.get("code");
        if (repo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code))
            throw AppException.badRequest("VAT definition code already exists: " + code);
        VatDefinition vd = new VatDefinition();
        vd.setTenantId(tenantId);
        vd.setCode(code);
        vd.setName((String) req.get("name"));
        vd.setRate(new BigDecimal(req.getOrDefault("rate", "0").toString()));
        vd.setTaxType((String) req.get("taxType"));
        vd.setAccountCode((String) req.get("accountCode"));
        vd.setDescription((String) req.get("description"));
        vd.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(vd))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update VAT definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        VatDefinition vd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("VAT definition not found: " + id));
        if (req.containsKey("name")) vd.setName((String) req.get("name"));
        if (req.containsKey("rate")) vd.setRate(new BigDecimal(req.get("rate").toString()));
        if (req.containsKey("taxType")) vd.setTaxType((String) req.get("taxType"));
        if (req.containsKey("accountCode")) vd.setAccountCode((String) req.get("accountCode"));
        if (req.containsKey("description")) vd.setDescription((String) req.get("description"));
        if (req.containsKey("active")) vd.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(vd))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete VAT definition")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        VatDefinition vd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("VAT definition not found: " + id));
        vd.setDeletedAt(LocalDateTime.now());
        repo.save(vd);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(VatDefinition v) {
        return Map.of(
                "id", v.getId(),
                "code", v.getCode(),
                "name", v.getName(),
                "rate", v.getRate(),
                "taxType", v.getTaxType() == null ? "" : v.getTaxType(),
                "accountCode", v.getAccountCode() == null ? "" : v.getAccountCode(),
                "description", v.getDescription() == null ? "" : v.getDescription(),
                "active", v.isActive(),
                "createdAt", v.getCreatedAt() == null ? "" : v.getCreatedAt().toString()
        );
    }
}
