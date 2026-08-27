package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateCustomerStockTransferRequest;
import com.erp.platform.modules.sales.dto.CustomerStockTransferDto;
import com.erp.platform.modules.sales.service.CustomerStockTransferService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/customer-stock-transfers")
@RequiredArgsConstructor
@Tag(name = "Customer Stock Transfers", description = "Customer stock transfer management")
public class CustomerStockTransferController {

    private final CustomerStockTransferService service;
    private final com.erp.platform.modules.sales.service.DealerTransferPostingService postingService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customer stock transfers")
    public ResponseEntity<ApiResponse<PageResponse<CustomerStockTransferDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("transferDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer stock transfer by ID")
    public ResponseEntity<ApiResponse<CustomerStockTransferDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create customer stock transfer")
    public ResponseEntity<ApiResponse<CustomerStockTransferDto>> create(@Valid @RequestBody CreateCustomerStockTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Customer stock transfer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update customer stock transfer")
    public ResponseEntity<ApiResponse<CustomerStockTransferDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCustomerStockTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete customer stock transfer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer stock transfer deleted"));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post the dealer transfer: from-customer sales return + to-customer stock issue, invoice & ledgers")
    public ResponseEntity<ApiResponse<CustomerStockTransferDto>> post(@PathVariable UUID id) {
        postingService.post(id);
        return ResponseEntity.ok(ApiResponse.success(service.findById(id),
                "Transfer posted: return credited to from-customer, invoice posted to to-customer"));
    }
}
