package com.erp.platform.modules.accounting.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.ShareCapital;
import com.erp.platform.modules.accounting.repository.ShareCapitalRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/share-capital")
@RequiredArgsConstructor
@Tag(name = "Accounting - Share Capital", description = "Share capital management")
public class ShareCapitalController {
    private final ShareCapitalRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "List share capital entries")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
            PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(page,size,Sort.by("createdAt").descending())).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary = "Create share capital entry")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tenantId = tenantContext.current();
        ShareCapital e = new ShareCapital();
        e.setTenantId(tenantId);
        // The screen calls this "Share Class"; the column is share_series and is NOT NULL, so a
        // create that only sent shareClass used to die on the insert.
        String shareClass = s(req, "shareClass", "shareSeries");
        if (shareClass == null) throw AppException.badRequest("Share Class is required");
        e.setShareSeries(shareClass);
        e.setShareType((String) req.get("shareType"));
        // The form posts "" for any number left blank, which Long.parseLong refuses.
        e.setAuthorizedShares(num(req.get("authorizedShares")));
        e.setIssuedShares(num(req.get("issuedShares")));
        e.setPaidUpShares(num(req.get("paidUpShares")));
        e.setFaceValue(dec(req.get("faceValue"), BigDecimal.ZERO));
        e.setPaidUpValue(dec(req.get("paidUpValue"), BigDecimal.ZERO));
        e.setIssueDate(date(req.get("issueDate")));
        e.setRemarks(s(req, "description", "remarks"));
        e.setActive(req.getOrDefault("active","true").toString().equals("true"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary = "Update share capital entry")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tenantId = tenantContext.current();
        ShareCapital e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).orElseThrow(() -> AppException.notFound("Not found"));
        String shareClass = s(req, "shareClass", "shareSeries");
        if (shareClass != null) e.setShareSeries(shareClass);
        if (req.containsKey("shareType")) e.setShareType((String) req.get("shareType"));
        if (num(req.get("authorizedShares")) != null) e.setAuthorizedShares(num(req.get("authorizedShares")));
        if (num(req.get("issuedShares")) != null) e.setIssuedShares(num(req.get("issuedShares")));
        if (num(req.get("paidUpShares")) != null) e.setPaidUpShares(num(req.get("paidUpShares")));
        if (req.containsKey("faceValue")) e.setFaceValue(dec(req.get("faceValue"), e.getFaceValue()));
        if (req.containsKey("paidUpValue")) e.setPaidUpValue(dec(req.get("paidUpValue"), e.getPaidUpValue()));
        if (req.containsKey("issueDate")) e.setIssueDate(date(req.get("issueDate")));
        if (req.containsKey("description") || req.containsKey("remarks")) e.setRemarks(s(req, "description", "remarks"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary = "Delete share capital entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tenantId = tenantContext.current();
        ShareCapital e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // Both spellings go out so the screen ("Share Class", "Description") and the column
    // (share_series, remarks) each find what they are looking for.
    private Map<String,Object> toMap(ShareCapital e) {
        Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("shareClass", e.getShareSeries()==null?"":e.getShareSeries());
        m.put("shareSeries", e.getShareSeries()==null?"":e.getShareSeries());
        m.put("shareType", e.getShareType()==null?"":e.getShareType());
        m.put("authorizedShares", e.getAuthorizedShares()==null?0L:e.getAuthorizedShares());
        m.put("issuedShares", e.getIssuedShares()==null?0L:e.getIssuedShares());
        m.put("paidUpShares", e.getPaidUpShares()==null?0L:e.getPaidUpShares());
        m.put("faceValue", e.getFaceValue());
        m.put("paidUpValue", e.getPaidUpValue());
        m.put("issueDate", e.getIssueDate()==null?null:e.getIssueDate().toString());
        m.put("description", e.getRemarks());
        m.put("remarks", e.getRemarks());
        m.put("active", e.isActive());
        m.put("createdAt", e.getCreatedAt()==null?"":e.getCreatedAt().toString());
        return m;
    }

    private static String s(Map<String,Object> r, String... keys) {
        for (String k : keys) { Object v = r.get(k); if (v != null && !v.toString().isBlank()) return v.toString(); }
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
