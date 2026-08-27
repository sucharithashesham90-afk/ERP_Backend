package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateExpenseRequest;
import com.erp.platform.modules.hr.dto.ExpenseDto;
import com.erp.platform.modules.hr.entity.Expense.ExpenseStatus;
import com.erp.platform.modules.hr.service.ExpenseService;
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

import java.util.Map;
import java.util.UUID;

@RestController("hrExpenseController")
@RequestMapping("/api/v1/hr/expenses")
@RequiredArgsConstructor
@Tag(name = "HR - Expenses", description = "Employee expense claim management")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List expense claims")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ExpenseStatus status,
            @RequestParam(required = false) UUID employeeId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(expenseService.list(status, employeeId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get expense claim by ID")
    public ResponseEntity<ApiResponse<ExpenseDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create expense claim")
    public ResponseEntity<ApiResponse<ExpenseDto>> create(@Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(expenseService.create(request), "Expense claim created"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update expense claim status (SUBMITTED / APPROVED / REJECTED / REIMBURSED)")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        ExpenseStatus status = ExpenseStatus.valueOf(body.get("status"));
        String rejectionReason = body.get("rejectionReason");
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateStatus(id, status, rejectionReason)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete expense claim (DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        expenseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense claim deleted"));
    }
}
