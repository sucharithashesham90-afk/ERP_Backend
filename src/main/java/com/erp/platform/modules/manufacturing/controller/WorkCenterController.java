package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.manufacturing.entity.WorkCenter;
import com.erp.platform.modules.manufacturing.service.WorkCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/manufacturing/work-centers")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Work Centers", description = "Work center / machine center master data")
public class WorkCenterController {

    private final WorkCenterService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List work centers")
    public ResponseEntity<ApiResponse<PageResponse<WorkCenter>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active work centers (for dropdowns)")
    public ResponseEntity<ApiResponse<List<WorkCenter>>> listActive() {
        return ResponseEntity.ok(ApiResponse.success(service.listActive()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkCenter>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create work center")
    public ResponseEntity<ApiResponse<WorkCenter>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(body), "Work center created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WorkCenter>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
