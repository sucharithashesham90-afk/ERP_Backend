package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.NetSalesClosing;
import com.erp.platform.modules.sales.repository.NetSalesClosingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales/net-sales-closing")
@RequiredArgsConstructor
@Tag(name = "Sales - Net Sales Closing", description = "Close the net sales of a season period")
public class NetSalesClosingController {

    private final NetSalesClosingRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List closed net-sales periods")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndDeletedAtIsNullOrderByClosedAtDesc(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Close the net sales of the selected season period")
    public ResponseEntity<ApiResponse<Map<String, Object>>> close(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String periodId = str(req, "seasonPeriodId");
        if (periodId == null || periodId.isBlank())
            throw AppException.badRequest("Season period is required");
        UUID seasonPeriodId = UUID.fromString(periodId);
        if (repo.findByTenantIdAndSeasonPeriodIdAndDeletedAtIsNull(tenantId, seasonPeriodId).isPresent())
            throw AppException.badRequest("Net sales for this period is already closed");

        NetSalesClosing e = new NetSalesClosing();
        e.setTenantId(tenantId);
        e.setSeasonPeriodId(seasonPeriodId);
        e.setSeasonPeriodName(str(req, "seasonPeriodName"));
        e.setClosed(true);
        e.setClosedAt(LocalDateTime.now());
        e.setClosedBy(currentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(e)), "Net sales closed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Reopen a closed net-sales period")
    public ResponseEntity<ApiResponse<Void>> reopen(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        NetSalesClosing e = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Closing not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String currentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String str(Map<String, Object> req, String key) {
        Object v = req.get(key);
        return v == null ? null : v.toString();
    }

    private Map<String, Object> toMap(NetSalesClosing e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("seasonPeriodId", e.getSeasonPeriodId() == null ? "" : e.getSeasonPeriodId().toString());
        m.put("seasonPeriodName", e.getSeasonPeriodName() == null ? "" : e.getSeasonPeriodName());
        m.put("closed", e.isClosed());
        m.put("closedAt", e.getClosedAt() == null ? "" : e.getClosedAt().toString());
        m.put("closedBy", e.getClosedBy() == null ? "" : e.getClosedBy());
        return m;
    }
}
