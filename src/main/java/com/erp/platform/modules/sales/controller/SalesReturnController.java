package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateSalesReturnRequest;
import com.erp.platform.modules.sales.dto.SalesReturnDto;
import com.erp.platform.modules.sales.service.SalesReturnService;
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
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/returns")
@RequiredArgsConstructor
@Tag(name = "Sales - Returns", description = "Sales return management")
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List sales returns")
    public ResponseEntity<ApiResponse<PageResponse<SalesReturnDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(salesReturnService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sales return by ID")
    public ResponseEntity<ApiResponse<SalesReturnDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesReturnService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sales return with item lines")
    public ResponseEntity<ApiResponse<SalesReturnDto>> create(@RequestBody CreateSalesReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(salesReturnService.create(request)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve return — restores stock for each returned item")
    public ResponseEntity<ApiResponse<SalesReturnDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(salesReturnService.approve(id)));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record payment / refund for sales return and generate Credit Note")
    public ResponseEntity<ApiResponse<SalesReturnDto>> recordPayment(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : BigDecimal.ZERO;
        String paymentMethod = body.get("paymentMethod") != null ? body.get("paymentMethod").toString() : "CASH";
        String chequeNumber = body.get("chequeNumber") != null ? body.get("chequeNumber").toString() : null;
        String chequeDate = body.get("chequeDate") != null ? body.get("chequeDate").toString() : null;
        return ResponseEntity.ok(ApiResponse.success(salesReturnService.recordPayment(id, amount, paymentMethod, chequeNumber, chequeDate)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete sales return (DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        salesReturnService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
