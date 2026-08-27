package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.service.EmployeeStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/employee-statuses")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EmployeeStatusController {

    private final EmployeeStatusService employeeStatusService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(employeeStatusService.list(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(employeeStatusService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeStatusService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(employeeStatusService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(employeeStatusService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        employeeStatusService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
