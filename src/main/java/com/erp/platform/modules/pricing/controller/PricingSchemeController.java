package com.erp.platform.modules.pricing.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.pricing.entity.PricingScheme;
import com.erp.platform.modules.pricing.repository.PricingSchemeRepository;
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
import java.util.Map; import java.util.UUID;

@RestController @RequestMapping("/api/v1/pricing/schemes")
@RequiredArgsConstructor @Tag(name="Pricing - Schemes",description="Pricing scheme management")
public class PricingSchemeController {
    private final PricingSchemeRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List pricing schemes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create pricing scheme")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PricingScheme e = new PricingScheme(); e.setTenantId(tid);
        e.setName((String) req.get("name")); e.setCode((String) req.get("code"));
        e.setDescription((String) req.get("description"));
        if (req.get("priceListId") != null) e.setPriceListId(UUID.fromString(req.get("priceListId").toString()));
        if (req.get("tiers") != null) e.setTiersJson(req.get("tiers").toString());
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update pricing scheme")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PricingScheme e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("name")) e.setName((String) req.get("name"));
        if (req.containsKey("tiers") && req.get("tiers") != null) e.setTiersJson(req.get("tiers").toString());
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete pricing scheme")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        PricingScheme e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(PricingScheme e) {
        return Map.of("id",e.getId(),"name",e.getName()==null?"":e.getName(),
            "code",e.getCode()==null?"":e.getCode(),
            "description",e.getDescription()==null?"":e.getDescription(),
            "tiersJson",e.getTiersJson()==null?"[]":e.getTiersJson(),
            "active",e.isActive(),"createdAt",e.getCreatedAt()==null?"":e.getCreatedAt().toString());
    }
}
