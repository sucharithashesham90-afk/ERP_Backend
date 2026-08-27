package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.PackingType;
import com.erp.platform.modules.purchase.repository.PackingTypeRepository;
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
@RequestMapping("/api/v1/purchase/packing-types")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PackingTypeController {

    private final PackingTypeRepository repository;
    private final TenantContext tenantContext;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PackingType>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable))));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<PackingType>> create(@RequestBody Map<String, Object> body) {
        if (body.get("name") == null || body.get("name").toString().isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION", "Name is required"));
        PackingType e = new PackingType();
        e.setTenantId(tenantContext.current());
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Packing type saved"));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<PackingType>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        PackingType e = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Packing type not found"));
        apply(e, body);
        return ResponseEntity.ok(ApiResponse.success(repository.save(e), "Packing type updated"));
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

    private void apply(PackingType e, Map<String, Object> body) {
        if (body.get("name") != null) e.setName(body.get("name").toString());
        if (body.containsKey("description")) e.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        if (body.get("active") != null) e.setActive(Boolean.parseBoolean(body.get("active").toString()));
    }
}
