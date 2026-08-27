package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.BudgetEntryDto;
import com.erp.platform.modules.accounting.dto.CreateBudgetEntryRequest;
import com.erp.platform.modules.accounting.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/budget")
@RequiredArgsConstructor
@Tag(name = "Accounting - Budget", description = "Budget planning and vs-actual tracking")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List budget entries for a period")
    public ResponseEntity<ApiResponse<PageResponse<BudgetEntryDto>>> list(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("accountCode"));
        return ResponseEntity.ok(ApiResponse.success(budgetService.list(year, month, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a budget entry")
    public ResponseEntity<ApiResponse<BudgetEntryDto>> create(@Valid @RequestBody CreateBudgetEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(budgetService.create(request), "Budget entry created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a budget entry")
    public ResponseEntity<ApiResponse<BudgetEntryDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBudgetEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.update(id, request), "Budget entry updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a budget entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        budgetService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget entry deleted successfully"));
    }

    @GetMapping("/vs-actual")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get budget vs actual summary for dashboard")
    public ResponseEntity<ApiResponse<List<BudgetEntryDto>>> vsActual(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.getBudgetVsActual(year, month)));
    }

    @PostMapping("/sync-actuals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sync actual amounts from posted GL journal entries for a given period")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> syncActuals(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(budgetService.syncActuals(year, month),
                "Actuals synced from GL"));
    }
}
