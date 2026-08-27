package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.PurchaseAppFeature;
import com.erp.platform.modules.purchase.repository.PurchaseAppFeatureRepository;
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
@RequestMapping("/api/v1/purchase/app-features")
@RequiredArgsConstructor
@Tag(name = "Purchase - App Features", description = "Purchase module feature flags")
public class PurchaseAppFeatureController {

    private final PurchaseAppFeatureRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase feature flags")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> get() {
        UUID tenantId = tenantContext.current();
        Map<String, Boolean> map = new LinkedHashMap<>();
        repo.findByTenantId(tenantId).forEach(f -> map.put(f.getFeatureKey(), f.isEnabled()));
        return ResponseEntity.ok(ApiResponse.success(map));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Transactional
    @Operation(summary = "Save purchase feature flags")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> save(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            boolean enabled = entry.getValue() != null && Boolean.parseBoolean(entry.getValue().toString());
            PurchaseAppFeature f = repo.findByTenantIdAndFeatureKey(tenantId, entry.getKey())
                    .orElseGet(() -> {
                        PurchaseAppFeature nf = new PurchaseAppFeature();
                        nf.setTenantId(tenantId);
                        nf.setFeatureKey(entry.getKey());
                        return nf;
                    });
            f.setEnabled(enabled);
            repo.save(f);
            result.put(entry.getKey(), enabled);
        }
        return ResponseEntity.ok(ApiResponse.success(result, "Purchase features saved"));
    }
}
