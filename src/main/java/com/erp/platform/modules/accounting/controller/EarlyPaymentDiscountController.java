package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.EarlyPaymentDiscount;
import com.erp.platform.modules.accounting.service.EarlyPaymentDiscountService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/early-payment-discounts")
@RequiredArgsConstructor
@Tag(name = "Accounting - Early Payment Discounts", description = "Early payment discount tracking and application")
public class EarlyPaymentDiscountController {

    private final EarlyPaymentDiscountService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List early payment discounts (applied or pending)")
    public ResponseEntity<ApiResponse<PageResponse<EarlyPaymentDiscount>>> list(
            @RequestParam(defaultValue = "false") boolean applied,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(service.list(applied, pageable)));
    }

    @GetMapping("/eligible")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get discounts eligible for application today")
    public ResponseEntity<ApiResponse<List<EarlyPaymentDiscount>>> getEligible() {
        return ResponseEntity.ok(ApiResponse.success(service.getEligible()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create early payment discount record")
    public ResponseEntity<ApiResponse<EarlyPaymentDiscount>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(body), "Early payment discount created"));
    }

    @PatchMapping("/{id}/apply")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Apply a discount (mark as used with voucher reference)")
    public ResponseEntity<ApiResponse<EarlyPaymentDiscount>> apply(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String voucherRef = body != null ? body.get("voucherReference") : null;
        return ResponseEntity.ok(ApiResponse.success(service.apply(id, voucherRef), "Discount applied"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete pending discount")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
