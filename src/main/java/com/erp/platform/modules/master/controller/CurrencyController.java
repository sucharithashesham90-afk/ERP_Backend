package com.erp.platform.modules.master.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Currency;
import com.erp.platform.modules.master.repository.CurrencyRepository;
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

@RestController @RequestMapping("/api/v1/master/currencies")
@RequiredArgsConstructor @Tag(name="Master - Currencies",description="Currency definition management")
public class CurrencyController {
    private final CurrencyRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List currencies")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("code"))).map(this::toMap))));
    }

    // The UI sends currencyCode/currencyName (typed Currency model); accept those (fallback code/name).
    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
        return null;
    }
    private static BigDecimal dec(Object v, BigDecimal def) {
        if (v == null || v.toString().isBlank()) return def;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException ex) { return def; }
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create currency")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        Currency e = new Currency(); e.setTenantId(tid);
        // currency_code has no unique constraint, so without this the same code could be added
        // twice and the Bank Account dropdown would list it twice with no way to tell them apart.
        String code = s(req, "currencyCode", "code");
        String name = s(req, "currencyName", "name");
        if (code == null) throw AppException.badRequest("Currency Code is required");
        if (name == null) throw AppException.badRequest("Currency Name is required");
        if (repo.existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(tid, code))
            throw AppException.conflict("Currency code already exists: " + code);
        e.setCode(code); e.setName(name);
        e.setSymbol((String) req.get("symbol"));
        if (req.get("decimalPlaces") != null) e.setDecimalPlaces((int) dec(req.get("decimalPlaces"), BigDecimal.valueOf(2)).intValue());
        e.setExchangeRate(dec(req.get("exchangeRate"), BigDecimal.ONE));
        e.setBaseCurrency(Boolean.parseBoolean(req.getOrDefault("isBaseCurrency","false").toString()));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update currency")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        Currency e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        String code = s(req, "currencyCode", "code");
        if (code != null) {
            if (repo.existsByTenantIdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(tid, code, id))
                throw AppException.conflict("Currency code already exists: " + code);
            e.setCode(code);
        }
        String name = s(req, "currencyName", "name"); if (name != null) e.setName(name);
        if (req.get("symbol") != null) e.setSymbol((String) req.get("symbol"));
        if (req.get("decimalPlaces") != null) e.setDecimalPlaces((int) dec(req.get("decimalPlaces"), BigDecimal.valueOf(e.getDecimalPlaces())).intValue());
        if (req.get("exchangeRate") != null) e.setExchangeRate(dec(req.get("exchangeRate"), e.getExchangeRate()));
        if (req.containsKey("isBaseCurrency")) e.setBaseCurrency(Boolean.parseBoolean(req.get("isBaseCurrency").toString()));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete currency")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        Currency e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(Currency e) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("code", e.getCode()==null?"":e.getCode());
        m.put("currencyCode", e.getCode()==null?"":e.getCode());
        m.put("name", e.getName()==null?"":e.getName());
        m.put("currencyName", e.getName()==null?"":e.getName());
        m.put("symbol", e.getSymbol()==null?"":e.getSymbol());
        m.put("decimalPlaces", e.getDecimalPlaces());
        m.put("exchangeRate", e.getExchangeRate());
        m.put("isBaseCurrency", e.isBaseCurrency());
        m.put("active", e.isActive());
        return m;
    }
}
