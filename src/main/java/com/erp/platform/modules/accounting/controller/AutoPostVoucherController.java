package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.service.AutoPostVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/auto-post-vouchers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AutoPostVoucherController {

    private final AutoPostVoucherService autoPostVoucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(autoPostVoucherService.list(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(autoPostVoucherService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(autoPostVoucherService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(autoPostVoucherService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        autoPostVoucherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
