package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.BankAccount;
import com.erp.platform.modules.accounting.entity.BankReconciliation;
import com.erp.platform.modules.accounting.entity.BankStatement;
import com.erp.platform.modules.accounting.service.BankReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accounting - Bank Reconciliation", description = "Bank account and reconciliation management")
public class BankReconciliationController {

    private final BankReconciliationService service;

    // ---- Bank Accounts ----

    @GetMapping("/api/v1/accounting/bank-accounts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bank accounts")
    public ResponseEntity<ApiResponse<PageResponse<BankAccount>>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listAccounts(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/accounting/bank-accounts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create bank account")
    public ResponseEntity<ApiResponse<BankAccount>> createAccount(@RequestBody BankAccount req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createAccount(req), "Created"));
    }

    @PutMapping("/api/v1/accounting/bank-accounts/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update bank account")
    public ResponseEntity<ApiResponse<BankAccount>> updateAccount(@PathVariable UUID id, @RequestBody BankAccount req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateAccount(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/bank-accounts/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete bank account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable UUID id) {
        service.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    // ---- Reconciliations ----

    @GetMapping("/api/v1/accounting/bank-reconciliations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bank reconciliations")
    public ResponseEntity<ApiResponse<PageResponse<BankReconciliation>>> listReconciliations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID accountId) {
        return ResponseEntity.ok(ApiResponse.success(service.listReconciliations(accountId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping("/api/v1/accounting/bank-reconciliations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create bank reconciliation")
    public ResponseEntity<ApiResponse<BankReconciliation>> createReconciliation(@RequestBody BankReconciliation req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.createReconciliation(req), "Created"));
    }

    @GetMapping("/api/v1/accounting/bank-reconciliations/{id}/entries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get statement entries for reconciliation")
    public ResponseEntity<ApiResponse<List<BankStatement>>> getEntries(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getEntries(id)));
    }

    @PostMapping("/api/v1/accounting/bank-reconciliations/{id}/entries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add statement entry")
    public ResponseEntity<ApiResponse<BankStatement>> addEntry(@PathVariable UUID id, @RequestBody BankStatement entry) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.addStatementEntry(id, entry), "Entry added"));
    }

    @PatchMapping("/api/v1/accounting/bank-reconciliations/{id}/entries/{entryId}/match")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Match a statement entry to a voucher")
    public ResponseEntity<ApiResponse<BankStatement>> matchEntry(
            @PathVariable UUID id,
            @PathVariable UUID entryId,
            @RequestBody(required = false) Map<String, UUID> body) {
        UUID voucherId = body != null ? body.get("voucherId") : null;
        return ResponseEntity.ok(ApiResponse.success(service.matchEntry(id, entryId, voucherId)));
    }

    @PatchMapping("/api/v1/accounting/bank-reconciliations/{id}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close reconciliation")
    public ResponseEntity<ApiResponse<BankReconciliation>> closeReconciliation(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.closeReconciliation(id)));
    }

    @PostMapping("/api/v1/accounting/bank-reconciliations/{id}/auto-match")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Auto-match statement entries to GL transactions by amount")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> autoMatch(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.autoMatch(id), "Auto-match completed"));
    }
}
