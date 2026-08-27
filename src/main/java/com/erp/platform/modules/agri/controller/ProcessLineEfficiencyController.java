package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessLineEfficiencyRequest;
import com.erp.platform.modules.agri.dto.ProcessLineEfficiencyDto;
import com.erp.platform.modules.agri.service.ProcessLineEfficiencyService;
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
@RequestMapping("/api/v1/agri/process-line-efficiency")
@RequiredArgsConstructor
@Tag(name = "Process Line Efficiency", description = "Process line efficiency tracking")
public class ProcessLineEfficiencyController {

    private final ProcessLineEfficiencyService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List process line efficiency records")
    public ResponseEntity<ApiResponse<PageResponse<ProcessLineEfficiencyDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get process line efficiency by ID")
    public ResponseEntity<ApiResponse<ProcessLineEfficiencyDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create process line efficiency record")
    public ResponseEntity<ApiResponse<ProcessLineEfficiencyDto>> create(@Valid @RequestBody CreateProcessLineEfficiencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Process line efficiency record created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update process line efficiency record")
    public ResponseEntity<ApiResponse<ProcessLineEfficiencyDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProcessLineEfficiencyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete process line efficiency record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Process line efficiency record deleted"));
    }
}
