package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateEmployeeRequest;
import com.erp.platform.modules.hr.dto.EmployeeDto;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.service.EmployeeService;
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
@RequestMapping("/api/v1/hr/employees")
@RequiredArgsConstructor
@Tag(name = "HR - Employees", description = "Employee management")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List employees")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) java.util.UUID departmentId) {
        var pageable = PageRequest.of(page, size, Sort.by("firstName"));
        return ResponseEntity.ok(ApiResponse.success(employeeService.list(search, departmentId, pageable)));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "The Employee profile linked to the current user, if any")
    public ResponseEntity<ApiResponse<EmployeeDto>> getCurrentEmployee() {
        EmployeeDto dto = employeeService.getCurrentEmployee();
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(employeeService.create(request), "Employee created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update employee")
    public ResponseEntity<ApiResponse<EmployeeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update employee status")
    public ResponseEntity<ApiResponse<EmployeeDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        EmployeeStatus status = EmployeeStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(employeeService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete employee (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deleted successfully"));
    }
}
