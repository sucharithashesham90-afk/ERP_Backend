package com.erp.platform.modules.master.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.UomCategory;
import com.erp.platform.modules.master.repository.UomCategoryRepository;
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

@RestController @RequestMapping("/api/v1/master/uom-categories")
@RequiredArgsConstructor @Tag(name="Master - UoM Categories",description="Unit of measure category management")
public class UomCategoryController {
    private final UomCategoryRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List UoM categories")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create UoM category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        UomCategory e = new UomCategory(); e.setTenantId(tid);
        e.setName(s(req, "categoryName", "name")); e.setCode(s(req, "categoryCode", "code"));
        e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update UoM category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        UomCategory e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        String name = s(req, "categoryName", "name"); if (name != null) e.setName(name);
        String code = s(req, "categoryCode", "code"); if (code != null) e.setCode(code);
        if (req.get("description") != null) e.setDescription((String) req.get("description"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete UoM category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        UomCategory e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // The UI sends categoryName/categoryCode (typed model); accept those (fallback name/code).
    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
        return null;
    }

    private Map<String,Object> toMap(UomCategory e) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName()==null?"":e.getName());
        m.put("categoryName", e.getName()==null?"":e.getName());
        m.put("code", e.getCode()==null?"":e.getCode());
        m.put("categoryCode", e.getCode()==null?"":e.getCode());
        m.put("description", e.getDescription()==null?"":e.getDescription());
        m.put("active", e.isActive());
        return m;
    }
}
