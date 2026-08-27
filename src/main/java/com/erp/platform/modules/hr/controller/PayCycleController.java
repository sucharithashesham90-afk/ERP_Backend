package com.erp.platform.modules.hr.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.PayCycle;
import com.erp.platform.modules.hr.repository.PayCycleRepository;
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

@RestController @RequestMapping("/api/v1/hr/pay-cycles")
@RequiredArgsConstructor @Tag(name="HR - Pay Cycles",description="Payroll cycle definitions")
public class PayCycleController {
    private final PayCycleRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List pay cycles")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create pay cycle")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PayCycle e = new PayCycle(); e.setTenantId(tid);
        e.setName((String) req.get("name")); e.setCode((String) req.get("code")); e.setFrequency((String) req.get("frequency"));
        if (req.get("startDayOfMonth") != null) e.setStartDayOfMonth(Integer.parseInt(req.get("startDayOfMonth").toString()));
        e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update pay cycle")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        PayCycle e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("name")) e.setName((String) req.get("name"));
        if (req.containsKey("code")) e.setCode((String) req.get("code"));
        if (req.containsKey("frequency")) e.setFrequency((String) req.get("frequency"));
        if (req.get("startDayOfMonth") != null) e.setStartDayOfMonth(Integer.parseInt(req.get("startDayOfMonth").toString()));
        if (req.containsKey("description")) e.setDescription((String) req.get("description"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete pay cycle")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        PayCycle e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String,Object> toMap(PayCycle e) {
        return Map.of("id",e.getId(),"name",e.getName()==null?"":e.getName(),
            "code",e.getCode()==null?"":e.getCode(),
            "frequency",e.getFrequency()==null?"":e.getFrequency(),
            "startDayOfMonth",e.getStartDayOfMonth()==null?1:e.getStartDayOfMonth(),
            "description",e.getDescription()==null?"":e.getDescription(),
            "active",e.isActive(),"createdAt",e.getCreatedAt()==null?"":e.getCreatedAt().toString());
    }
}
