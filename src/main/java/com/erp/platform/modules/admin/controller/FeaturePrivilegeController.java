package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.admin.entity.FeaturePrivilege;
import com.erp.platform.modules.admin.repository.FeaturePrivilegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Feature Privileges — grant a feature to a group or user, and revoke it.
 */
@RestController
@RequestMapping("/api/v1/admin/feature-privileges")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FeaturePrivilegeController {

    private final FeaturePrivilegeRepository repository;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeaturePrivilege>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable))));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<FeaturePrivilege>> grant(@RequestBody Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        String featureKey = str(body, "featureKey");
        String subjectType = str(body, "subjectType");
        String subjectIdStr = str(body, "subjectId");
        if (featureKey == null || subjectType == null || subjectIdStr == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "featureKey, subjectType and subjectId are required"));
        }
        UUID subjectId = UUID.fromString(subjectIdStr);
        // Upsert — one grant per (feature, subjectType, subject).
        FeaturePrivilege fp = repository
                .findByTenantIdAndFeatureKeyAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(tenantId, featureKey, subjectType, subjectId)
                .orElseGet(() -> {
                    FeaturePrivilege e = new FeaturePrivilege();
                    e.setTenantId(tenantId);
                    e.setFeatureKey(featureKey);
                    e.setSubjectType(subjectType);
                    e.setSubjectId(subjectId);
                    return e;
                });
        fp.setFeatureName(str(body, "featureName"));
        fp.setSubjectName(str(body, "subjectName"));
        fp.setGranted(true);
        return ResponseEntity.ok(ApiResponse.success(repository.save(fp), "Access granted"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).ifPresent(fp -> {
            fp.setDeletedAt(LocalDateTime.now());
            repository.save(fp);
        });
        return ResponseEntity.ok(ApiResponse.success(null, "Access removed"));
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null && !v.toString().isBlank() ? v.toString() : null;
    }
}
