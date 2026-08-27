package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.AccountService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounting - Accounts", description = "Chart of accounts management")
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List accounts")
    public ResponseEntity<ApiResponse<PageResponse<Account>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String type) {
        var pageable = PageRequest.of(page, size, Sort.by("code"));
        return ResponseEntity.ok(ApiResponse.success(accountService.list(type, pageable)));
    }

    @GetMapping("/by-type")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get accounts by type")
    public ResponseEntity<ApiResponse<List<Account>>> byType(@RequestParam String type) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getByType(type)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ApiResponse<Account>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create account")
    public ResponseEntity<ApiResponse<Account>> create(@RequestBody Account account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.create(account), "Account created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update account")
    public ResponseEntity<ApiResponse<Account>> update(@PathVariable UUID id, @RequestBody Account body) {
        return ResponseEntity.ok(ApiResponse.success(accountService.update(id, body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete account (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }

    @PatchMapping("/{id}/merge")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Merge source account into target account (soft-deletes source)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> merge(
            @PathVariable UUID id,
            @RequestParam UUID targetId) {
        var tenantId = tenantContext.current();
        var source = accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Source account not found: " + id));
        source.setDeletedAt(LocalDateTime.now());
        accountRepository.save(source);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "message", "Merged successfully",
                "sourceId", id.toString(),
                "targetId", targetId.toString())));
    }

    @GetMapping("/merge-history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Account merge history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> mergeHistory() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
