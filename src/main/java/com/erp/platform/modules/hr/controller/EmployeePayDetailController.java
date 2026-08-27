package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateEmployeePayDetailRequest;
import com.erp.platform.modules.hr.dto.EmployeePayDetailDto;
import com.erp.platform.modules.hr.service.EmployeePayDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/employee-pay-details")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeePayDetailController {

    private final EmployeePayDetailService employeePayDetailService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeePayDetailDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(employeePayDetailService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeePayDetailDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeePayDetailService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeePayDetailDto>> create(@Valid @RequestBody CreateEmployeePayDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeePayDetailService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeePayDetailDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEmployeePayDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employeePayDetailService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeePayDetailService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
