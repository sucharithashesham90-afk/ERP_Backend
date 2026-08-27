package com.erp.platform.modules.hr.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.StatutoryDeduction;
import com.erp.platform.modules.hr.repository.StatutoryDeductionRepository;
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

@RestController @RequestMapping("/api/v1/hr/statutory-deductions")
@RequiredArgsConstructor @Tag(name="HR - Statutory Deductions",description="Statutory deduction rules")
public class StatutoryDeductionController {
    private final StatutoryDeductionRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List statutory deductions")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create statutory deduction")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        StatutoryDeduction e = new StatutoryDeduction(); e.setTenantId(tid);
        e.setName((String) req.get("name")); e.setCode((String) req.get("code")); e.setDeductionType((String) req.get("deductionType"));
        if (req.get("employeeRate") != null) e.setEmployeeRate(new BigDecimal(req.get("employeeRate").toString()));
        if (req.get("employerRate") != null) e.setEmployerRate(new BigDecimal(req.get("employerRate").toString()));
        if (req.get("ceiling") != null) e.setCeiling(new BigDecimal(req.get("ceiling").toString()));
        if (req.get("effectiveFrom") != null && !req.get("effectiveFrom").toString().isBlank()) e.setEffectiveFrom(LocalDate.parse(req.get("effectiveFrom").toString()));
        if (req.get("effectiveTo") != null && !req.get("effectiveTo").toString().isBlank()) e.setEffectiveTo(LocalDate.parse(req.get("effectiveTo").toString()));
        e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update statutory deduction")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        StatutoryDeduction e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("name")) e.setName((String) req.get("name"));
        if (req.containsKey("code")) e.setCode((String) req.get("code"));
        if (req.containsKey("deductionType")) e.setDeductionType((String) req.get("deductionType"));
        if (req.containsKey("employeeRate") && req.get("employeeRate") != null) e.setEmployeeRate(new BigDecimal(req.get("employeeRate").toString()));
        if (req.containsKey("employerRate") && req.get("employerRate") != null) e.setEmployerRate(new BigDecimal(req.get("employerRate").toString()));
        if (req.containsKey("ceiling") && req.get("ceiling") != null) e.setCeiling(new BigDecimal(req.get("ceiling").toString()));
        if (req.containsKey("effectiveFrom") && req.get("effectiveFrom") != null && !req.get("effectiveFrom").toString().isBlank()) e.setEffectiveFrom(LocalDate.parse(req.get("effectiveFrom").toString()));
        if (req.containsKey("effectiveTo") && req.get("effectiveTo") != null && !req.get("effectiveTo").toString().isBlank()) e.setEffectiveTo(LocalDate.parse(req.get("effectiveTo").toString()));
        if (req.containsKey("description")) e.setDescription((String) req.get("description"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete statutory deduction")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        StatutoryDeduction e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(StatutoryDeduction e) {
        return Map.ofEntries(
            Map.entry("id",e.getId()),
            Map.entry("name",e.getName()==null?"":e.getName()),
            Map.entry("code",e.getCode()==null?"":e.getCode()),
            Map.entry("deductionType",e.getDeductionType()==null?"":e.getDeductionType()),
            Map.entry("employeeRate",e.getEmployeeRate()),
            Map.entry("employerRate",e.getEmployerRate()),
            Map.entry("ceiling",e.getCeiling()),
            Map.entry("effectiveFrom",e.getEffectiveFrom()==null?"":e.getEffectiveFrom().toString()),
            Map.entry("effectiveTo",e.getEffectiveTo()==null?"":e.getEffectiveTo().toString()),
            Map.entry("description",e.getDescription()==null?"":e.getDescription()),
            Map.entry("active",e.isActive())
        );
    }
}
