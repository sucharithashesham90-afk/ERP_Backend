package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.PeriodCloseDto;
import com.erp.platform.modules.accounting.service.PeriodCloseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounting/period-close")
@RequiredArgsConstructor
@Tag(name = "Accounting - Period Close", description = "Accounting period close management")
public class PeriodCloseController {

    private final PeriodCloseService periodCloseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List period close records for a year")
    public ResponseEntity<ApiResponse<PageResponse<PeriodCloseDto>>> list(
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("periodMonth"));
        return ResponseEntity.ok(ApiResponse.success(periodCloseService.list(year, pageable)));
    }

    @GetMapping("/{year}/{month}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get period close status for a specific month")
    public ResponseEntity<ApiResponse<PeriodCloseDto>> getByPeriod(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(ApiResponse.success(periodCloseService.getByPeriod(year, month)));
    }

    @PostMapping("/{year}/{month}/initiate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initiate period close process")
    public ResponseEntity<ApiResponse<PeriodCloseDto>> initiate(
            @PathVariable int year,
            @PathVariable int month,
            Principal principal) {
        String closedBy = principal != null ? principal.getName() : "system";
        return ResponseEntity.ok(ApiResponse.success(
                periodCloseService.initiatePeriodClose(year, month, closedBy),
                "Period close initiated"));
    }

    @PostMapping("/{year}/{month}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete period close")
    public ResponseEntity<ApiResponse<PeriodCloseDto>> complete(
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(ApiResponse.success(
                periodCloseService.completePeriodClose(year, month),
                "Period close completed"));
    }

    @PostMapping("/{year}/{month}/reopen")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reopen a closed period")
    public ResponseEntity<ApiResponse<PeriodCloseDto>> reopen(
            @PathVariable int year,
            @PathVariable int month,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "") : "";
        return ResponseEntity.ok(ApiResponse.success(
                periodCloseService.reopenPeriod(year, month, reason),
                "Period reopened"));
    }

    @PostMapping("/{year}/{month}/freeze")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Freeze a closed period (no further posting allowed)")
    public ResponseEntity<ApiResponse<PeriodCloseDto>> freeze(
            @PathVariable int year,
            @PathVariable int month,
            @RequestBody(required = false) Map<String, String> body) {
        String frozenBy = body != null ? body.getOrDefault("frozenBy", "System") : "System";
        return ResponseEntity.ok(ApiResponse.success(
                periodCloseService.freezePeriod(year, month, frozenBy),
                "Period frozen"));
    }

    @PostMapping("/post-ledger-balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Post each ledger's balance as the next period's opening balance (dated 1st April)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> postLedgerBalance() {
        return ResponseEntity.ok(ApiResponse.success(
                periodCloseService.postLedgerBalance(), "Ledger balances posted to next period"));
    }
}
