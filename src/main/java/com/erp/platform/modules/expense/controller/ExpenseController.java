package com.erp.platform.modules.expense.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.expense.entity.Expense;
import com.erp.platform.modules.expense.entity.Expense.ExpenseStatus;
import com.erp.platform.modules.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Employee expense claims and approvals")
public class ExpenseController {

    private final ExpenseService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List expense claims")
    public ResponseEntity<ApiResponse<PageResponse<Expense>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        ExpenseStatus st = status != null ? ExpenseStatus.valueOf(status) : null;
        return ResponseEntity.ok(ApiResponse.success(service.list(st, employeeId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Expense>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create expense claim")
    public ResponseEntity<ApiResponse<Expense>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(body), "Expense created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Expense>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, body)));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit expense for approval")
    public ResponseEntity<ApiResponse<Expense>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.submit(id), "Expense submitted"));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve expense claim")
    public ResponseEntity<ApiResponse<Expense>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id), "Expense approved"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject expense claim")
    public ResponseEntity<ApiResponse<Expense>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(service.reject(id, reason), "Expense rejected"));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark expense as paid")
    public ResponseEntity<ApiResponse<Expense>> markPaid(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String method = body != null ? body.getOrDefault("paymentMethod", "BANK_TRANSFER") : "BANK_TRANSFER";
        return ResponseEntity.ok(ApiResponse.success(service.markPaid(id, method), "Expense paid"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
