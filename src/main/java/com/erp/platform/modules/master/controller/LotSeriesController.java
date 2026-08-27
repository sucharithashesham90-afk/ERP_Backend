package com.erp.platform.modules.master.controller;
import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.LotSeries;
import com.erp.platform.modules.master.repository.LotSeriesRepository;
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

@RestController @RequestMapping("/api/v1/master/lot-series")
@RequiredArgsConstructor @Tag(name="Master - Lot Series",description="Lot number series management")
public class LotSeriesController {
    private final LotSeriesRepository repo;
    private final TenantContext tenantContext;

    @GetMapping @PreAuthorize("isAuthenticated()") @Operation(summary="List lot series")
    public ResponseEntity<ApiResponse<PageResponse<Map<String,Object>>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var tid = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(
            repo.findByTenantIdAndDeletedAtIsNull(tid, PageRequest.of(page,size,Sort.by("name"))).map(this::toMap))));
    }

    @PostMapping @PreAuthorize("isAuthenticated()") @Operation(summary="Create lot series")
    public ResponseEntity<ApiResponse<Map<String,Object>>> create(@RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        LotSeries e = new LotSeries(); e.setTenantId(tid);
        e.setName((String) req.get("name")); e.setPrefix((String) req.get("prefix"));
        e.setSuffix((String) req.get("suffix"));
        if (req.get("nextNumber") != null) e.setNextNumber(Long.parseLong(req.get("nextNumber").toString()));
        if (req.get("incrementBy") != null) e.setIncrementBy(Integer.parseInt(req.get("incrementBy").toString()));
        if (req.get("padding") != null) e.setPadding(Integer.parseInt(req.get("padding").toString()));
        e.setDescription((String) req.get("description"));
        e.setActive(Boolean.parseBoolean(req.getOrDefault("active","true").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e))));
    }

    @PutMapping("/{id}") @PreAuthorize("isAuthenticated()") @Operation(summary="Update lot series")
    public ResponseEntity<ApiResponse<Map<String,Object>>> update(@PathVariable UUID id, @RequestBody Map<String,Object> req) {
        var tid = tenantContext.current();
        LotSeries e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        if (req.containsKey("name")) e.setName((String) req.get("name"));
        if (req.containsKey("active")) e.setActive(Boolean.parseBoolean(req.get("active").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(e))));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") @Operation(summary="Delete lot series")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        var tid = tenantContext.current();
        LotSeries e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        e.setDeletedAt(LocalDateTime.now()); repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/allocate") @PreAuthorize("isAuthenticated()") @Operation(summary="Allocate next lot number")
    public ResponseEntity<ApiResponse<Map<String,Object>>> allocate(@PathVariable UUID id) {
        var tid = tenantContext.current();
        LotSeries e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tid,id).orElseThrow(() -> AppException.notFound("Not found"));
        long num = e.getNextNumber();
        int pad = e.getPadding() == null ? 4 : e.getPadding();
        String formatted = String.format("%0" + pad + "d", num);
        String allocated = (e.getPrefix()==null?"":e.getPrefix()) + formatted + (e.getSuffix()==null?"":e.getSuffix());
        e.setNextNumber(num + (e.getIncrementBy()==null?1:e.getIncrementBy()));
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(Map.of("allocatedNumber",allocated,"nextNumber",e.getNextNumber())));
    }

    private Map<String,Object> toMap(LotSeries e) {
        return Map.of("id",e.getId(),"name",e.getName()==null?"":e.getName(),
            "prefix",e.getPrefix()==null?"":e.getPrefix(),"suffix",e.getSuffix()==null?"":e.getSuffix(),
            "nextNumber",e.getNextNumber()==null?1L:e.getNextNumber(),
            "padding",e.getPadding()==null?4:e.getPadding(),"active",e.isActive());
    }
}
