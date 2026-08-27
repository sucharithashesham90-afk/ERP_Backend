package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.LedgerOpeningBalance;
import com.erp.platform.modules.accounting.repository.LedgerOpeningBalanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Location-wise opening balances that belong to a single ledger (Account).
 */
@RestController
@RequestMapping("/api/v1/accounting/accounts/{accountId}/opening-balances")
@RequiredArgsConstructor
@Tag(name = "Accounting - Ledger Opening Balances", description = "Per-ledger, per-location opening balances")
public class LedgerOpeningBalanceController {

    private final LedgerOpeningBalanceRepository repo;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List a ledger's location-wise opening balances")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list(@PathVariable UUID accountId) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndAccountIdAndDeletedAtIsNull(tenantId, accountId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    /** Replace-all: the submitted list becomes the ledger's complete set of opening balances. */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Replace a ledger's location-wise opening balances")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> replace(
            @PathVariable UUID accountId, @RequestBody List<Map<String, Object>> rows) {
        UUID tenantId = tenantContext.current();

        // Reject duplicate locations up-front.
        Set<String> seen = new HashSet<>();
        for (Map<String, Object> r : rows) {
            String loc = str(r, "location");
            if (loc != null && !seen.add(loc.toLowerCase()))
                throw AppException.badRequest("Duplicate location in opening balances: " + loc);
        }

        // Soft-delete the existing set, then persist the new one.
        var existing = repo.findByTenantIdAndAccountIdAndDeletedAtIsNull(tenantId, accountId);
        existing.forEach(e -> e.setDeletedAt(LocalDateTime.now()));
        repo.saveAll(existing);

        List<LedgerOpeningBalance> saved = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            LedgerOpeningBalance ob = new LedgerOpeningBalance();
            ob.setTenantId(tenantId);
            ob.setAccountId(accountId);
            ob.setLocation(str(r, "location"));
            ob.setSubAccount(bool(r, "subAccount"));
            ob.setOpeningBalance(r.get("openingBalance") == null ? BigDecimal.ZERO
                    : new BigDecimal(r.get("openingBalance").toString()));
            ob.setBalanceType(r.get("balanceType") == null ? "DEBIT" : str(r, "balanceType"));
            String date = str(r, "openBalDate");
            if (date != null && !date.isBlank()) ob.setOpenBalDate(LocalDate.parse(date));
            ob.setVisible(r.containsKey("visible") ? bool(r, "visible") : true);
            saved.add(repo.save(ob));
        }
        return ResponseEntity.ok(ApiResponse.success(saved.stream().map(this::toMap).collect(Collectors.toList())));
    }

    private static String str(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? null : v.toString();
    }

    private static boolean bool(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v != null && Boolean.parseBoolean(v.toString());
    }

    private Map<String, Object> toMap(LedgerOpeningBalance o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("location", o.getLocation() == null ? "" : o.getLocation());
        m.put("subAccount", o.isSubAccount());
        m.put("openingBalance", o.getOpeningBalance() == null ? BigDecimal.ZERO : o.getOpeningBalance());
        m.put("balanceType", o.getBalanceType() == null ? "DEBIT" : o.getBalanceType());
        m.put("openBalDate", o.getOpenBalDate() == null ? "" : o.getOpenBalDate().toString());
        m.put("visible", o.isVisible());
        return m;
    }
}
