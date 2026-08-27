package com.erp.platform.modules.master.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.VatDefinition;
import com.erp.platform.modules.accounting.repository.VatDefinitionRepository;
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

@RestController @RequestMapping("/api/v1/master/vat-categories")
@RequiredArgsConstructor @Tag(name="Master - VAT Categories",description="VAT/GST category master data")
public class MasterVatCategoryController {
    private final VatDefinitionRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List VAT categories")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create VAT category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        VatDefinition e = new VatDefinition(); e.setTenantId(tid);
        // UI sends categoryCode/categoryName + cgst/sgst/igst/cess/hsn; accept those (fallback old names).
        e.setCode(s(req, "categoryCode", "code")); e.setName(s(req, "categoryName", "name"));
        e.setTaxType((String) req.get("taxType"));
        applyRates(e, req);
        e.setHsnCode((String) req.get("hsnCode"));
        e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
        return null;
    }
    private static BigDecimal dec(Object v) {
        if (v == null || v.toString().isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException ex) { return BigDecimal.ZERO; }
    }
    private static void applyRates(VatDefinition e, Map<String,Object> req) {
        e.setCgstRate(dec(req.get("cgstRate")));
        e.setSgstRate(dec(req.get("sgstRate")));
        e.setIgstRate(dec(req.get("igstRate")));
        e.setCessRate(dec(req.get("cessRate")));
        // Combined rate = IGST if given, else CGST + SGST, else explicit rate.
        BigDecimal combined = e.getIgstRate().signum() > 0 ? e.getIgstRate()
                : (e.getCgstRate().add(e.getSgstRate()).signum() > 0 ? e.getCgstRate().add(e.getSgstRate())
                : dec(req.get("rate")));
        e.setRate(combined);
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update VAT category")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        VatDefinition e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        String code = s(req, "categoryCode", "code"); if (code != null) e.setCode(code);
        String name = s(req, "categoryName", "name"); if (name != null) e.setName(name);
        if (req.get("taxType") != null) e.setTaxType((String) req.get("taxType"));
        applyRates(e, req);
        if (req.get("hsnCode") != null) e.setHsnCode((String) req.get("hsnCode"));
        if (req.get("description") != null) e.setDescription((String) req.get("description"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete VAT category")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        VatDefinition e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(VatDefinition e) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("code", e.getCode()==null?"":e.getCode()); m.put("categoryCode", e.getCode()==null?"":e.getCode());
        m.put("name", e.getName()==null?"":e.getName()); m.put("categoryName", e.getName()==null?"":e.getName());
        m.put("taxType", e.getTaxType()==null?"":e.getTaxType());
        m.put("rate", e.getRate());
        m.put("cgstRate", e.getCgstRate()); m.put("sgstRate", e.getSgstRate());
        m.put("igstRate", e.getIgstRate()); m.put("cessRate", e.getCessRate());
        m.put("hsnCode", e.getHsnCode()==null?"":e.getHsnCode());
        m.put("description", e.getDescription()==null?"":e.getDescription());
        m.put("active", e.isActive());
        return m;
    }
}
