package com.erp.platform.modules.payroll.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.payroll.entity.Payslip;
import com.erp.platform.modules.payroll.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.erp.platform.modules.payroll.entity.SalaryComponent;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/payslips")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll and payslip management")
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List payslips")
    public ResponseEntity<ApiResponse<PageResponse<Payslip>>> list(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(payrollService.listPayslips(month, year, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payslip by ID")
    public ResponseEntity<ApiResponse<Payslip>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayslipById(id)));
    }

    @PostMapping("/run")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk generate payslips for all active employees for a given month/year")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runPayroll(@RequestBody Map<String, Object> body) {
        int month = Integer.parseInt(body.get("month").toString());
        int year = Integer.parseInt(body.get("year").toString());
        return ResponseEntity.ok(ApiResponse.success(payrollService.runPayroll(month, year), "Payroll run completed"));
    }

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate payslip for an employee")
    public ResponseEntity<ApiResponse<Payslip>> generate(@RequestBody Map<String, Object> body) {
        UUID empId = UUID.fromString(body.get("employeeId").toString());
        int month = Integer.parseInt(body.get("month").toString());
        int year = Integer.parseInt(body.get("year").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.generatePayslip(empId, month, year), "Payslip generated"));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve payslip")
    public ResponseEntity<ApiResponse<Payslip>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.approve(id), "Payslip approved"));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark payslip as paid")
    public ResponseEntity<ApiResponse<Payslip>> markPaid(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                payrollService.markPaid(id, body.getOrDefault("paymentMethod", "BANK_TRANSFER")),
                "Marked as paid"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject a DRAFT payslip")
    public ResponseEntity<ApiResponse<Payslip>> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Rejected");
        return ResponseEntity.ok(ApiResponse.success(payrollService.reject(id, reason), "Payslip rejected"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a payslip (any non-PAID status)")
    public ResponseEntity<ApiResponse<Payslip>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.cancel(id), "Payslip cancelled"));
    }

    // ---- Salary Components ----

    @GetMapping("/salary-components")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List salary components")
    public ResponseEntity<ApiResponse<PageResponse<SalaryComponent>>> listComponents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(ApiResponse.success(payrollService.listComponents(pageable)));
    }

    @PostMapping("/salary-components")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create salary component")
    public ResponseEntity<ApiResponse<SalaryComponent>> createComponent(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.createComponent(body), "Created"));
    }

    @PutMapping("/salary-components/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update salary component")
    public ResponseEntity<ApiResponse<SalaryComponent>> updateComponent(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updateComponent(id, body)));
    }

    @DeleteMapping("/salary-components/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete salary component")
    public ResponseEntity<ApiResponse<Void>> deleteComponent(@PathVariable UUID id) {
        payrollService.deleteComponent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
