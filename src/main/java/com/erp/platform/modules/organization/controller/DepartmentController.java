package com.erp.platform.modules.organization.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.organization.dto.CreateDepartmentRequest;
import com.erp.platform.modules.organization.dto.DepartmentDto;
import com.erp.platform.modules.organization.service.DepartmentService;
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
@RequestMapping("/api/v1/org/departments")
@RequiredArgsConstructor
@Tag(name = "Organization - Departments", description = "Department management")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List departments")
    public ResponseEntity<ApiResponse<PageResponse<DepartmentDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID companyId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(departmentService.list(companyId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<DepartmentDto>> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(departmentService.create(request), "Department created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update department")
    public ResponseEntity<ApiResponse<DepartmentDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete department (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Department deleted successfully"));
    }
}
