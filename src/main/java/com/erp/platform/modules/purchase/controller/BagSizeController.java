package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.BagSize;
import com.erp.platform.modules.purchase.repository.BagSizeRepository;
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

@RestController
@RequestMapping("/api/v1/purchase/bag-sizes")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BagSizeController {

    private final BagSizeRepository repository;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BagSize>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable))));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<BagSize>> create(@RequestBody Map<String, Object> body) {
        if (body.get("size") == null || body.get("size").toString().isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "Size is required"));
        BagSize e = new BagSize();
        e.setTenantId(tenantContext.current());
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Bag size saved"));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<BagSize>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        BagSize e = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Bag size not found"));
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Bag size updated"));
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

    private void apply(BagSize e, Map<String, Object> body) {
        if (body.get("bagId") != null && !body.get("bagId").toString().isBlank()) e.setBagId(UUID.fromString(body.get("bagId").toString()));
        if (body.containsKey("bagName")) e.setBagName(body.get("bagName") != null ? body.get("bagName").toString() : null);
        if (body.get("size") != null) e.setSize(body.get("size").toString());
        if (body.containsKey("purchaseUnitUomId")) {
            Object uom = body.get("purchaseUnitUomId");
            e.setPurchaseUnitUomId(uom != null && !uom.toString().isBlank() ? UUID.fromString(uom.toString()) : null);
        }
        if (body.containsKey("purchaseUnit")) e.setPurchaseUnit(body.get("purchaseUnit") != null ? body.get("purchaseUnit").toString() : null);
        if (body.get("active") != null) e.setActive(Boolean.parseBoolean(body.get("active").toString()));
    }
}
