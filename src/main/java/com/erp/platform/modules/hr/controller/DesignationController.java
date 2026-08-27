package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.service.DesignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/designations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DesignationController {

    private final DesignationService designationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(designationService.list(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(designationService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(designationService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(designationService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(designationService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        designationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
