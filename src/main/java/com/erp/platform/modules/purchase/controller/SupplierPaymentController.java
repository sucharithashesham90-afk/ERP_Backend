package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreateSupplierPaymentRequest;
import com.erp.platform.modules.purchase.dto.SupplierPaymentDto;
import com.erp.platform.modules.purchase.entity.SupplierPayment.PayStatus;
import com.erp.platform.modules.purchase.service.SupplierPaymentService;
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
@RequestMapping("/api/v1/purchase/supplier-payments")
@RequiredArgsConstructor
@Tag(name = "Purchase - Supplier Payments", description = "Supplier payment tracking")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List supplier payments")
    public ResponseEntity<ApiResponse<PageResponse<SupplierPaymentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) PayStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.list(vendorId, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get supplier payment by ID")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create supplier payment")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> create(@RequestBody CreateSupplierPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(supplierPaymentService.create(request), "Supplier payment created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update supplier payment (DRAFT only)")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateSupplierPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.update(id, request), "Supplier payment updated"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve supplier payment")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                supplierPaymentService.approve(id, body.get("approvedBy")), "Payment approved"));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Process supplier payment")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> process(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.process(id), "Payment processed"));
    }

    @PostMapping("/{id}/reconcile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reconcile supplier payment")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> reconcile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.reconcile(id), "Payment reconciled"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel supplier payment")
    public ResponseEntity<ApiResponse<SupplierPaymentDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierPaymentService.cancel(id), "Payment cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete supplier payment (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        supplierPaymentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Supplier payment deleted successfully"));
    }
}
