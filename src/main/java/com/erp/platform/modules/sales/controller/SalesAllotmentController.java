package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.entity.SalesAllotment;
import com.erp.platform.modules.sales.entity.SalesAllotment.AllotmentStatus;
import com.erp.platform.modules.sales.service.SalesAllotmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/allotments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SalesAllotmentController {

    private final SalesAllotmentService allotmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SalesAllotment>>> list(
            @RequestParam(required = false) AllotmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                allotmentService.list(status, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesAllotment>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(allotmentService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SalesAllotment>> create(@RequestBody SalesAllotment req) {
        return ResponseEntity.ok(ApiResponse.success(allotmentService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesAllotment>> update(@PathVariable UUID id, @RequestBody SalesAllotment req) {
        return ResponseEntity.ok(ApiResponse.success(allotmentService.update(id, req)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SalesAllotment>> updateStatus(
            @PathVariable UUID id, @RequestBody java.util.Map<String, String> body) {
        AllotmentStatus newStatus = AllotmentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(allotmentService.updateStatus(id, newStatus)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        allotmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
