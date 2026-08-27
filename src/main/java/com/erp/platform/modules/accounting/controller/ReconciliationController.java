package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.accounting.dto.ReconciliationDto;
import com.erp.platform.modules.accounting.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Accounting - Reconciliation", description = "AR/AP reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping("/ar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AR summary — all customers with open invoices")
    public ResponseEntity<ApiResponse<ReconciliationDto.ArSummary>> arSummary() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getArSummary()));
    }

    @GetMapping("/ar/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AR detail — open invoices for a specific customer")
    public ResponseEntity<ApiResponse<ReconciliationDto.ArDetail>> arDetail(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getArDetail(customerId)));
    }

    @GetMapping("/ap")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AP summary — all vendors with outstanding payables")
    public ResponseEntity<ApiResponse<ReconciliationDto.ApSummary>> apSummary() {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getApSummary()));
    }

    @GetMapping("/ap/{vendorId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "AP detail — outstanding purchase invoices for a specific vendor")
    public ResponseEntity<ApiResponse<ReconciliationDto.ApDetail>> apDetail(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.success(reconciliationService.getApDetail(vendorId)));
    }
}
