package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.PaymentTerm;
import com.erp.platform.modules.accounting.service.PaymentTermService;
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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accounting - Payment Terms", description = "Payment terms and early payment discount management")
public class PaymentTermController {

    private final PaymentTermService service;

    // ---- Payment Terms ----

    @GetMapping("/api/v1/accounting/payment-terms")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List payment terms")
    public ResponseEntity<ApiResponse<PageResponse<PaymentTerm>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/api/v1/accounting/payment-terms/all-active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all active payment terms for dropdown")
    public ResponseEntity<ApiResponse<List<PaymentTerm>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActive()));
    }

    @PostMapping("/api/v1/accounting/payment-terms")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create payment term")
    public ResponseEntity<ApiResponse<PaymentTerm>> create(@RequestBody PaymentTerm req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/api/v1/accounting/payment-terms/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update payment term")
    public ResponseEntity<ApiResponse<PaymentTerm>> update(@PathVariable UUID id, @RequestBody PaymentTerm req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/api/v1/accounting/payment-terms/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete payment term")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

}
