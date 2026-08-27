package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.purchase.entity.PackingMaterial;
import com.erp.platform.modules.purchase.repository.PackingMaterialRepository;
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

/** Generic + Product Specific packing materials (Purchase Configuration). */
@RestController
@RequestMapping("/api/v1/purchase/packing-materials")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PackingMaterialController {

    private final PackingMaterialRepository repository;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PackingMaterial>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) Boolean productSpecific) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID tenantId = tenantContext.current();
        var result = productSpecific == null
                ? repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                : productSpecific
                    ? repository.findByTenantIdAndProductIdIsNotNullAndDeletedAtIsNull(tenantId, pageable)
                    : repository.findByTenantIdAndProductIdIsNullAndDeletedAtIsNull(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<PackingMaterial>> create(@RequestBody Map<String, Object> body) {
        if (PayloadUtils.str(body, "name") == null || PayloadUtils.str(body, "name").isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "Name is required"));
        PackingMaterial e = new PackingMaterial();
        e.setTenantId(tenantContext.current());
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Packing material saved"));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<PackingMaterial>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        PackingMaterial e = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Packing material not found"));
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Packing material updated"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id).ifPresent(e -> {
            e.setDeletedAt(LocalDateTime.now());
            repository.save(e);
        });
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    private void apply(PackingMaterial e, Map<String, Object> body) {
        if (body.get("name") != null) e.setName(PayloadUtils.str(body, "name"));
        e.setPackingTypeId(PayloadUtils.uuid(body, "packingTypeId"));
        e.setPackingTypeName(PayloadUtils.str(body, "packingTypeName"));
        e.setTechnicalSpecification(PayloadUtils.str(body, "technicalSpecification"));
        e.setApproxUnitWeightGms(PayloadUtils.str(body, "approxUnitWeightGms"));
        e.setApproxUnitCost(PayloadUtils.str(body, "approxUnitCost"));
        e.setPacketsPerKg(PayloadUtils.str(body, "packetsPerKg"));
        e.setUsageType(PayloadUtils.str(body, "usageType"));
        e.setTypeId(PayloadUtils.uuid(body, "typeId"));
        e.setTypeName(PayloadUtils.str(body, "typeName"));
        e.setPackingSizeId(PayloadUtils.uuid(body, "packingSizeId"));
        e.setPackingSizeName(PayloadUtils.str(body, "packingSizeName"));
        e.setImagePath(PayloadUtils.str(body, "imagePath"));
        e.setPackCapacitiesJson(PayloadUtils.str(body, "packCapacitiesJson"));
        e.setProductId(PayloadUtils.uuid(body, "productId"));
        e.setProductName(PayloadUtils.str(body, "productName"));
        if (body.get("active") != null) e.setActive(Boolean.parseBoolean(body.get("active").toString()));
    }
}
