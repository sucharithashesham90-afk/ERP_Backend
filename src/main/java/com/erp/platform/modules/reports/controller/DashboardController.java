package com.erp.platform.modules.reports.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard KPIs and summary")
public class DashboardController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get dashboard KPI summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboardSummary()));
    }

    @GetMapping("/sales-trend")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get monthly sales trend for a given year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> salesTrend(
            @RequestParam(required = false) Integer year) {
        // Defaulted to a literal 2026, which would quietly start returning an empty year every
        // 1 January. The current year is the only sensible default.
        int y = year != null ? year : java.time.LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlySalesTrend(y)));
    }

    @GetMapping("/top-products")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Best-selling products by invoiced revenue")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> topProducts(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getTopProducts(limit, parseDate(from), parseDate(to))));
    }

    @GetMapping("/top-customers")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Highest-billed customers by invoiced revenue")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> topCustomers(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getTopCustomers(limit, parseDate(from), parseDate(to))));
    }

    @GetMapping("/financial-position")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cash, book balances, receivable and payable off the chart of accounts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> financialPosition() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getFinancialPosition()));
    }

    /** A bad date in the query string falls back to the default window rather than 400-ing a chart. */
    private static java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return java.time.LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
