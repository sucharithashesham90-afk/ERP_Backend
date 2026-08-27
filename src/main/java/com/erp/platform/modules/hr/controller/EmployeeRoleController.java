package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.entity.EmployeeRole;
import com.erp.platform.modules.hr.repository.EmployeeRoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/employee-roles")
@RequiredArgsConstructor
@Tag(name = "HR - Employee Roles", description = "Employee group and role assignments")
public class EmployeeRoleController {

    private final EmployeeRoleRepository employeeRoleRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List employee role assignments")
    public ResponseEntity<ApiResponse<List<EmployeeRole>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                employeeRoleRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get employee role by ID")
    public ResponseEntity<ApiResponse<EmployeeRole>> getById(@PathVariable UUID id) {
        EmployeeRole entity = employeeRoleRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Employee role not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign employee role")
    public ResponseEntity<ApiResponse<EmployeeRole>> create(@RequestBody EmployeeRole req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.ok(ApiResponse.success(employeeRoleRepository.save(req), "Assigned"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update employee role assignment")
    public ResponseEntity<ApiResponse<EmployeeRole>> update(@PathVariable UUID id, @RequestBody EmployeeRole req) {
        EmployeeRole e = employeeRoleRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Employee role not found: " + id));
        e.setEmployeeName(req.getEmployeeName());
        e.setGroupName(req.getGroupName());
        e.setRoleName(req.getRoleName());
        e.setEffectiveDate(req.getEffectiveDate());
        return ResponseEntity.ok(ApiResponse.success(employeeRoleRepository.save(e), "Updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Remove employee role assignment")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        EmployeeRole e = employeeRoleRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Employee role not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        employeeRoleRepository.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed"));
    }
}
