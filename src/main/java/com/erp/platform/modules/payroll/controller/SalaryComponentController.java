package com.erp.platform.modules.payroll.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.payroll.entity.SalaryComponent;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/salary-components")
@RequiredArgsConstructor
@Tag(name = "Payroll - Salary Components", description = "Salary component configuration")
public class SalaryComponentController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List salary components")
    public ResponseEntity<ApiResponse<PageResponse<SalaryComponent>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(ApiResponse.success(payrollService.listComponents(pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create salary component")
    public ResponseEntity<ApiResponse<SalaryComponent>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(payrollService.createComponent(body), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update salary component")
    public ResponseEntity<ApiResponse<SalaryComponent>> update(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.updateComponent(id, body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete salary component")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        payrollService.deleteComponent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
