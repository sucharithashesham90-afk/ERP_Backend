package com.erp.platform.modules.shareholder.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.shareholder.dto.CouponDto;
import com.erp.platform.modules.shareholder.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shareholder/coupons")
@RequiredArgsConstructor
@Tag(name = "Share Coupons", description = "Share Capital — Dividend coupon issuance and tracking")
public class CouponController {

    private final CouponService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List coupons")
    public ResponseEntity<ApiResponse<PageResponse<CouponDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("issueDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<ApiResponse<CouponDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @GetMapping("/by-shareholder/{shareholderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List coupons for a specific shareholder")
    public ResponseEntity<ApiResponse<List<CouponDto>>> byShareholder(@PathVariable UUID shareholderId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByShareHolder(shareholderId)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Issue a new coupon")
    public ResponseEntity<ApiResponse<CouponDto>> issue(@RequestBody CouponDto req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.issue(req), "Coupon issued"));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark coupon as paid")
    public ResponseEntity<ApiResponse<CouponDto>> markPaid(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate paidDate,
            @RequestParam(required = false) String paymentReference) {
        return ResponseEntity.ok(ApiResponse.success(
                service.markPaid(id, paidDate, paymentReference), "Coupon marked as paid"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel coupon")
    public ResponseEntity<ApiResponse<CouponDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancel(id), "Coupon cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete coupon")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted"));
    }
}
