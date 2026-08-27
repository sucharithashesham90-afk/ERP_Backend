package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.InventoryAppFeature;
import com.erp.platform.modules.inventory.repository.InventoryAppFeatureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/app-features")
@RequiredArgsConstructor
@Tag(name = "Inventory - App Features", description = "Inventory module feature flags")
public class InventoryAppFeatureController {

    private final InventoryAppFeatureRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get inventory feature flags")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> get() {
        UUID tenantId = tenantContext.current();
        Map<String, Boolean> map = new LinkedHashMap<>();
        repo.findByTenantId(tenantId).forEach(f -> map.put(f.getFeatureKey(), f.isEnabled()));
        return ResponseEntity.ok(ApiResponse.success(map));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Transactional
    @Operation(summary = "Save inventory feature flags")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> save(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            boolean enabled = entry.getValue() != null && Boolean.parseBoolean(entry.getValue().toString());
            InventoryAppFeature f = repo.findByTenantIdAndFeatureKey(tenantId, entry.getKey())
                    .orElseGet(() -> {
                        InventoryAppFeature nf = new InventoryAppFeature();
                        nf.setTenantId(tenantId);
                        nf.setFeatureKey(entry.getKey());
                        return nf;
                    });
            f.setEnabled(enabled);
            repo.save(f);
            result.put(entry.getKey(), enabled);
        }
        return ResponseEntity.ok(ApiResponse.success(result, "Inventory features saved"));
    }
}
