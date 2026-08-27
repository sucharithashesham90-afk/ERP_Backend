package com.erp.platform.modules.hr.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.SalaryElement;
import com.erp.platform.modules.hr.repository.SalaryElementRepository;
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

@RestController @RequestMapping("/api/v1/hr/salary-elements")
@RequiredArgsConstructor @Tag(name="HR - Salary Elements",description="Salary element definitions")
public class SalaryElementController {
    private final SalaryElementRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List salary elements")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create salary element")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        SalaryElement e = new SalaryElement(); e.setTenantId(tid);
        e.setName((String) req.get("name")); e.setCode((String) req.get("code"));
        e.setElementType((String) req.get("elementType")); e.setCalculationType((String) req.get("calculationType"));
        if (req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        if (req.get("percentage") != null) e.setPercentage(new BigDecimal(req.get("percentage").toString()));
        e.setFormula((String) req.get("formula")); e.setDescription((String) req.get("description"));
        e.setPercentageBase((String) req.get("percentageBase"));
        e.setTaxable(Boolean.parseBoolean(req.getOrDefault("taxable","false").toString()));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update salary element")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        SalaryElement e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("name")) e.setName((String) req.get("name"));
        if (req.containsKey("code")) e.setCode((String) req.get("code"));
        if (req.containsKey("elementType")) e.setElementType((String) req.get("elementType"));
        if (req.containsKey("calculationType")) e.setCalculationType((String) req.get("calculationType"));
        if (req.containsKey("amount") && req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        if (req.containsKey("percentage") && req.get("percentage") != null) e.setPercentage(new BigDecimal(req.get("percentage").toString()));
        if (req.containsKey("percentageBase")) e.setPercentageBase((String) req.get("percentageBase"));
        if (req.containsKey("formula")) e.setFormula((String) req.get("formula"));
        if (req.containsKey("description")) e.setDescription((String) req.get("description"));
        if (req.containsKey("taxable")) e.setTaxable(Boolean.parseBoolean(req.get("taxable").toString()));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete salary element")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        SalaryElement e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Map.of is not used here: it caps at ten pairs and rejects nulls, and percentageBase is null
     * for a fixed-amount element.
     */
    private Map<String,Object> toMap(SalaryElement e) {
        Map<String,Object> m = new java.util.HashMap<>();
        m.put("id", e.getId());
        m.put("name", e.getName()==null?"":e.getName());
        m.put("code", e.getCode()==null?"":e.getCode());
        m.put("elementType", e.getElementType()==null?"":e.getElementType());
        m.put("calculationType", e.getCalculationType()==null?"":e.getCalculationType());
        m.put("amount", e.getAmount());
        m.put("percentage", e.getPercentage());
        m.put("percentageBase", e.getPercentageBase()==null?"":e.getPercentageBase());
        // The grid shows one Value column, so the figure that applies is resolved here rather than
        // leaving the screen to pick between amount and percentage — it read neither, so the column
        // was always blank.
        m.put("value", "PERCENTAGE".equals(e.getCalculationType()) ? e.getPercentage() : e.getAmount());
        m.put("taxable", e.isTaxable());
        m.put("active", e.isActive());
        return m;
    }
}
