package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePurchaseInvoiceRequest;
import com.erp.platform.modules.purchase.dto.PurchaseInvoiceDto;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus;
import com.erp.platform.modules.purchase.service.PurchaseInvoiceService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase/invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoices", description = "Vendor invoice management with TDS support")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase invoices")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseInvoiceDto>>> list(
            @RequestParam(required = false) PIStatus status,
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            service.list(status, vendorId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase invoice by ID")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase invoice")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> create(
            @Valid @RequestBody CreatePurchaseInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update purchase invoice")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePurchaseInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve purchase invoice")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id)));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record payment against purchase invoice")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> recordPayment(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return ResponseEntity.ok(ApiResponse.success(service.recordPayment(id, amount)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete (soft) purchase invoice")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
