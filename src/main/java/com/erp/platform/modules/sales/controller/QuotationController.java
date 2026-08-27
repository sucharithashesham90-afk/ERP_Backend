package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateQuotationRequest;
import com.erp.platform.modules.sales.dto.QuotationDto;
import com.erp.platform.modules.sales.dto.SalesOrderDto;
import com.erp.platform.modules.sales.entity.Quotation.QuotationStatus;
import com.erp.platform.modules.sales.service.QuotationService;
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

@RestController
@RequestMapping("/api/v1/sales/quotations")
@RequiredArgsConstructor
@Tag(name = "Sales - Quotations", description = "Quotation management")
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List quotations")
    public ResponseEntity<ApiResponse<PageResponse<QuotationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) QuotationStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(quotationService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get quotation by ID")
    public ResponseEntity<ApiResponse<QuotationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create quotation")
    public ResponseEntity<ApiResponse<QuotationDto>> create(@Valid @RequestBody CreateQuotationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quotationService.create(request), "Quotation created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quotation (DRAFT or SENT only)")
    public ResponseEntity<ApiResponse<QuotationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuotationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quotation status")
    public ResponseEntity<ApiResponse<QuotationDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        QuotationStatus status = QuotationStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(quotationService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send quotation to customer (marks as SENT and emails)")
    public ResponseEntity<ApiResponse<QuotationDto>> send(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(quotationService.sendToCustomer(id), "Quotation sent to customer"));
    }

    @PostMapping("/{id}/convert-to-order")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Convert quotation to sales order")
    public ResponseEntity<ApiResponse<SalesOrderDto>> convertToOrder(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quotationService.convertToOrder(id), "Order created from quotation"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete quotation (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        quotationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Quotation deleted successfully"));
    }
}
