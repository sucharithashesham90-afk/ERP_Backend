package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePurchaseQuotationRequest;
import com.erp.platform.modules.purchase.dto.PurchaseOrderDto;
import com.erp.platform.modules.purchase.dto.PurchaseQuotationDto;
import com.erp.platform.modules.purchase.entity.PurchaseQuotation.PQStatus;
import com.erp.platform.modules.purchase.service.PurchaseQuotationService;
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
@RequestMapping("/api/v1/purchase/quotations")
@RequiredArgsConstructor
@Tag(name = "Purchase - Quotations", description = "Purchase quotation management")
public class PurchaseQuotationController {

    private final PurchaseQuotationService quotationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase quotations")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseQuotationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID requisitionId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(quotationService.list(requisitionId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase quotation by ID")
    public ResponseEntity<ApiResponse<PurchaseQuotationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase quotation")
    public ResponseEntity<ApiResponse<PurchaseQuotationDto>> create(
            @RequestBody CreatePurchaseQuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quotationService.create(request), "Purchase quotation created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update purchase quotation")
    public ResponseEntity<ApiResponse<PurchaseQuotationDto>> update(
            @PathVariable UUID id,
            @RequestBody CreatePurchaseQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.update(id, request), "Purchase quotation updated"));
    }

    @PostMapping("/{id}/select")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Select this quotation as the winning quotation")
    public ResponseEntity<ApiResponse<PurchaseQuotationDto>> select(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.selectQuotation(id), "Quotation selected"));
    }

    @PostMapping("/{id}/create-po")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Select quotation and create a Purchase Order from it")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createPo(@PathVariable UUID id) {
        PurchaseOrderDto po = quotationService.createPoFromQuotation(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(po, "Purchase Order " + po.getPoNumber() + " created from quotation"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quotation status")
    public ResponseEntity<ApiResponse<PurchaseQuotationDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        PQStatus status = PQStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(quotationService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete purchase quotation (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        quotationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase quotation deleted successfully"));
    }
}
