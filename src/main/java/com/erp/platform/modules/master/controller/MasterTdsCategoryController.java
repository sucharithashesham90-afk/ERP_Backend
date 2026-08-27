package com.erp.platform.modules.master.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.TdsCategory;
import com.erp.platform.modules.accounting.repository.TdsCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDateTime;
import java.util.Map; import java.util.UUID;

@RestController @RequestMapping("/api/v1/master/tds-categories")
@RequiredArgsConstructor @Tag(name="Master - TDS Categories",description="TDS category master data")
public class MasterTdsCategoryController {
    private final TdsCategoryRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List TDS categories")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create TDS category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        TdsCategory e = new TdsCategory(); e.setTenantId(tid);
        // UI sends categoryName/tdsRate/cessRate/section (typed model); accept those (fallback old names).
        e.setName(s(req, "categoryName", "name"));
        e.setRate(dec(firstVal(req, "tdsRate", "rate")));
        e.setSurchargeRate(dec(req.get("surchargeRate")));
        e.setEducationCessRate(dec(firstVal(req, "cessRate", "educationCessRate")));
        e.setSectionCode(s(req, "section", "sectionCode")); e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
        return null;
    }
    private static Object firstVal(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v; }
        return null;
    }
    private static BigDecimal dec(Object v) {
        if (v == null || v.toString().isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update TDS category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        TdsCategory e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        String name = s(req, "categoryName", "name"); if (name != null) e.setName(name);
        Object rate = firstVal(req, "tdsRate", "rate"); if (rate != null) e.setRate(dec(rate));
        if (req.get("surchargeRate") != null) e.setSurchargeRate(dec(req.get("surchargeRate")));
        Object cess = firstVal(req, "cessRate", "educationCessRate"); if (cess != null) e.setEducationCessRate(dec(cess));
        String section = s(req, "section", "sectionCode"); if (section != null) e.setSectionCode(section);
        if (req.get("description") != null) e.setDescription((String) req.get("description"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete TDS category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        TdsCategory e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(TdsCategory t) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName()); m.put("categoryName", t.getName());
        m.put("rate", t.getRate()); m.put("tdsRate", t.getRate());
        m.put("surchargeRate", t.getSurchargeRate());
        m.put("educationCessRate", t.getEducationCessRate()); m.put("cessRate", t.getEducationCessRate());
        m.put("sectionCode", t.getSectionCode()==null?"":t.getSectionCode());
        m.put("section", t.getSectionCode()==null?"":t.getSectionCode());
        m.put("description", t.getDescription()==null?"":t.getDescription());
        m.put("active", t.isActive());
        return m;
    }
}
