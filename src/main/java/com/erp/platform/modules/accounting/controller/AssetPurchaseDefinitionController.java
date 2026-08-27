package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.AssetPurchaseDefinition;
import com.erp.platform.modules.accounting.repository.AssetPurchaseDefinitionRepository;
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
@RequestMapping("/api/v1/accounting/asset-purchase-definitions")
@RequiredArgsConstructor
@Tag(name = "Accounting - Asset Purchase Definitions", description = "Asset purchase policy and GL account mapping")
public class AssetPurchaseDefinitionController {

    private final AssetPurchaseDefinitionRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List asset purchase definitions")
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
    @Operation(summary = "List active asset purchase definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create asset purchase definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String code = (String) req.get("code");
        if (repo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code))
            throw AppException.badRequest("Asset purchase definition code already exists: " + code);
        AssetPurchaseDefinition apd = new AssetPurchaseDefinition();
        apd.setTenantId(tenantId);
        apd.setCode(code);
        apd.setName((String) req.get("name"));
        apd.setAssetGroupCode((String) req.get("assetGroupCode"));
        apd.setPurchaseAccountCode((String) req.get("purchaseAccountCode"));
        apd.setAccDepreciationAccountCode((String) req.get("accDepreciationAccountCode"));
        apd.setDepreciationExpenseCode((String) req.get("depreciationExpenseCode"));
        apd.setMinCapitalizationAmount(new BigDecimal(req.getOrDefault("minCapitalizationAmount", "0").toString()));
        apd.setDescription((String) req.get("description"));
        apd.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(apd))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update asset purchase definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        AssetPurchaseDefinition apd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Asset purchase definition not found: " + id));
        if (req.containsKey("name")) apd.setName((String) req.get("name"));
        if (req.containsKey("assetGroupCode")) apd.setAssetGroupCode((String) req.get("assetGroupCode"));
        if (req.containsKey("purchaseAccountCode")) apd.setPurchaseAccountCode((String) req.get("purchaseAccountCode"));
        if (req.containsKey("accDepreciationAccountCode")) apd.setAccDepreciationAccountCode((String) req.get("accDepreciationAccountCode"));
        if (req.containsKey("depreciationExpenseCode")) apd.setDepreciationExpenseCode((String) req.get("depreciationExpenseCode"));
        if (req.containsKey("minCapitalizationAmount")) apd.setMinCapitalizationAmount(new BigDecimal(req.get("minCapitalizationAmount").toString()));
        if (req.containsKey("description")) apd.setDescription((String) req.get("description"));
        if (req.containsKey("active")) apd.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(apd))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete asset purchase definition")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        AssetPurchaseDefinition apd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Asset purchase definition not found: " + id));
        apd.setDeletedAt(LocalDateTime.now());
        repo.save(apd);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(AssetPurchaseDefinition a) {
        java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("code", a.getCode());
        m.put("name", a.getName());
        m.put("assetGroupCode", a.getAssetGroupCode() == null ? "" : a.getAssetGroupCode());
        m.put("purchaseAccountCode", a.getPurchaseAccountCode() == null ? "" : a.getPurchaseAccountCode());
        m.put("accDepreciationAccountCode", a.getAccDepreciationAccountCode() == null ? "" : a.getAccDepreciationAccountCode());
        m.put("depreciationExpenseCode", a.getDepreciationExpenseCode() == null ? "" : a.getDepreciationExpenseCode());
        m.put("minCapitalizationAmount", a.getMinCapitalizationAmount());
        m.put("description", a.getDescription() == null ? "" : a.getDescription());
        m.put("active", a.isActive());
        m.put("createdAt", a.getCreatedAt() == null ? "" : a.getCreatedAt().toString());
        return m;
    }
}
