package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateCustomerAdvanceRequest;
import com.erp.platform.modules.sales.dto.CustomerAdvanceDto;
import com.erp.platform.modules.sales.service.CustomerAdvanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/advances")
@RequiredArgsConstructor
@Tag(name = "Customer Advances", description = "Record and apply customer advance payments")
public class CustomerAdvanceController {

    private final CustomerAdvanceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customer advances")
    public ResponseEntity<ApiResponse<PageResponse<CustomerAdvanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer advance by ID")
    public ResponseEntity<ApiResponse<CustomerAdvanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/customer/{customerId}/available")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get total available advance balance for a customer")
    public ResponseEntity<ApiResponse<BigDecimal>> availableBalance(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAvailableBalance(customerId)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record a customer advance payment")
    public ResponseEntity<ApiResponse<CustomerAdvanceDto>> create(
            @Valid @RequestBody CreateCustomerAdvanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PostMapping("/apply/{invoiceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Apply available advances to an invoice")
    public ResponseEntity<ApiResponse<BigDecimal>> applyToInvoice(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(service.applyAdvancesToInvoice(invoiceId)));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark advance as refunded")
    public ResponseEntity<ApiResponse<CustomerAdvanceDto>> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.refund(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete customer advance")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
