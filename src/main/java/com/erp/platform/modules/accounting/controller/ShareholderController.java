package com.erp.platform.modules.accounting.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Shareholder;
import com.erp.platform.modules.accounting.repository.ShareholderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.LocalDateTime;
import java.util.Map; import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/shareholders")
@RequiredArgsConstructor
@Tag(name = "Accounting - Shareholders", description = "Shareholder register management")
public class ShareholderController {
    private final ShareholderRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "List shareholders")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "Create shareholder")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tenantId = tenantContext.current();
        Shareholder e = new Shareholder();
        e.setTenantId(tenantId);
        // The screen says share count / share value / contact info; the columns say shares_held /
        // holding_percentage / address. Accept either spelling, and treat "" as absent.
        String name = s(req, "name");
        if (name == null) throw AppException.badRequest("Name is required");
        e.setName(name);
        e.setEmail(s(req, "email"));
        e.setPhone(s(req, "phone"));
        e.setPanNumber(s(req, "panNumber"));
        e.setFolioNumber(s(req, "folioNumber"));
        e.setSharesHeld(num(first(req, "shareCount", "sharesHeld")));
        e.setHoldingPercentage(dec(first(req, "shareValue", "holdingPercentage"), BigDecimal.ZERO));
        e.setAllotmentDate(date(req.get("allotmentDate")));
        e.setAddress(s(req, "contactInfo", "address"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary = "Update shareholder")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tenantId = tenantContext.current();
        Shareholder e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).orElseThrow(() -> AppException.notFound("Not found"));
        String name = s(req, "name");
        if (name != null) e.setName(name);
        if (req.containsKey("email")) e.setEmail(s(req, "email"));
        if (req.containsKey("phone")) e.setPhone(s(req, "phone"));
        if (req.containsKey("panNumber")) e.setPanNumber(s(req, "panNumber"));
        if (req.containsKey("folioNumber")) e.setFolioNumber(s(req, "folioNumber"));
        if (req.containsKey("shareCount") || req.containsKey("sharesHeld"))
            e.setSharesHeld(num(first(req, "shareCount", "sharesHeld")));
        if (req.containsKey("shareValue") || req.containsKey("holdingPercentage"))
            e.setHoldingPercentage(dec(first(req, "shareValue", "holdingPercentage"), e.getHoldingPercentage()));
        if (req.containsKey("contactInfo") || req.containsKey("address")) e.setAddress(s(req, "contactInfo", "address"));
        if (req.containsKey("allotmentDate")) e.setAllotmentDate(date(req.get("allotmentDate")));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary = "Delete shareholder")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tenantId = tenantContext.current();
        Shareholder e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(Shareholder e) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName()==null?"":e.getName());
        m.put("email", e.getEmail()==null?"":e.getEmail());
        m.put("phone", e.getPhone()==null?"":e.getPhone());
        m.put("panNumber", e.getPanNumber()==null?"":e.getPanNumber());
        m.put("folioNumber", e.getFolioNumber()==null?"":e.getFolioNumber());
        m.put("shareCount", e.getSharesHeld()==null?0L:e.getSharesHeld());
        m.put("sharesHeld", e.getSharesHeld()==null?0L:e.getSharesHeld());
        m.put("shareValue", e.getHoldingPercentage());
        m.put("holdingPercentage", e.getHoldingPercentage());
        m.put("contactInfo", e.getAddress()==null?"":e.getAddress());
        m.put("address", e.getAddress()==null?"":e.getAddress());
        m.put("allotmentDate", e.getAllotmentDate()==null?null:e.getAllotmentDate().toString());
        m.put("active", e.isActive());
        return m;
    }

    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
        return null;
    }
    private static Object first(Map<String,Object> r, String... keys) {
        for (String k : keys) { if (r.containsKey(k)) return r.get(k); }
        return null;
    }
    private static Long num(Object v) {
        if (v == null || v.toString().isBlank()) return null;
        try { return new BigDecimal(v.toString()).longValue(); } catch (NumberFormatException ex) { return null; }
    }
    private static BigDecimal dec(Object v, BigDecimal def) {
        if (v == null || v.toString().isBlank()) return def;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException ex) { return def; }
    }
    private static LocalDate date(Object v) {
        if (v == null || v.toString().isBlank()) return null;
        try { return LocalDate.parse(v.toString()); } catch (Exception ex) { return null; }
    }
}
