package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.ClosingStockBalance;
import com.erp.platform.modules.accounting.repository.ClosingStockBalanceRepository;
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
@RequestMapping("/api/v1/accounting/closing-stock-balances")
@RequiredArgsConstructor
@Tag(name = "Accounting - Closing Stock Balances", description = "Period-end closing stock balance entries")
public class ClosingStockBalanceController {

    private final ClosingStockBalanceRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List closing stock balances")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "balanceDate"))).map(this::toMap))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create closing stock balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ClosingStockBalance csb = new ClosingStockBalance();
        csb.setTenantId(tenantId);
        csb.setPeriodCode((String) req.get("periodCode"));
        csb.setPeriodName((String) req.get("periodName"));
        csb.setAccountCode((String) req.get("accountCode"));
        csb.setAccountName((String) req.get("accountName"));
        csb.setClosingBalance(new BigDecimal(req.getOrDefault("closingBalance", "0").toString()));
        String dateStr = (String) req.get("balanceDate");
        csb.setBalanceDate(dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now());
        csb.setStatus((String) req.getOrDefault("status", "DRAFT"));
        csb.setNarration((String) req.get("narration"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(csb))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update closing stock balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        ClosingStockBalance csb = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Closing stock balance not found: " + id));
        if (csb.getStatus() != null && "FINALIZED".equals(csb.getStatus()))
            throw AppException.badRequest("Cannot edit a finalized closing stock balance");
        if (req.containsKey("periodCode")) csb.setPeriodCode((String) req.get("periodCode"));
        if (req.containsKey("periodName")) csb.setPeriodName((String) req.get("periodName"));
        if (req.containsKey("accountCode")) csb.setAccountCode((String) req.get("accountCode"));
        if (req.containsKey("accountName")) csb.setAccountName((String) req.get("accountName"));
        if (req.containsKey("closingBalance")) csb.setClosingBalance(new BigDecimal(req.get("closingBalance").toString()));
        if (req.containsKey("balanceDate")) csb.setBalanceDate(LocalDate.parse(req.get("balanceDate").toString()));
        if (req.containsKey("narration")) csb.setNarration((String) req.get("narration"));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(csb))));
    }

    @PatchMapping("/{id}/finalize")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Finalize closing stock balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> finalize(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ClosingStockBalance csb = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Closing stock balance not found: " + id));
        csb.setStatus("FINALIZED");
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(csb))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete closing stock balance")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        ClosingStockBalance csb = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Closing stock balance not found: " + id));
        if ("FINALIZED".equals(csb.getStatus()))
            throw AppException.badRequest("Cannot delete a finalized closing stock balance");
        csb.setDeletedAt(LocalDateTime.now());
        repo.save(csb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(ClosingStockBalance c) {
        java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("periodCode", c.getPeriodCode());
        m.put("periodName", c.getPeriodName() == null ? "" : c.getPeriodName());
        m.put("accountCode", c.getAccountCode());
        m.put("accountName", c.getAccountName() == null ? "" : c.getAccountName());
        m.put("closingBalance", c.getClosingBalance());
        m.put("balanceDate", c.getBalanceDate() == null ? "" : c.getBalanceDate().toString());
        m.put("status", c.getStatus() == null ? "DRAFT" : c.getStatus());
        m.put("narration", c.getNarration() == null ? "" : c.getNarration());
        m.put("createdAt", c.getCreatedAt() == null ? "" : c.getCreatedAt().toString());
        return m;
    }
}
