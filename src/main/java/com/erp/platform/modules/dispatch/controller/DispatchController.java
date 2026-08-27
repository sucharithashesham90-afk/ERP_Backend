package com.erp.platform.modules.dispatch.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.dispatch.dto.CreateDispatchRequest;
import com.erp.platform.modules.dispatch.dto.DispatchDto;
import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchStatus;
import com.erp.platform.modules.dispatch.service.DispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dispatch")
@RequiredArgsConstructor
@Tag(name = "Dispatch Management", description = "Manage dispatches and deliveries")
public class DispatchController {

    private final DispatchService dispatchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List dispatches")
    public ResponseEntity<ApiResponse<PageResponse<DispatchDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) DispatchStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(dispatchService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get dispatch by ID")
    public ResponseEntity<ApiResponse<DispatchDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create dispatch")
    public ResponseEntity<ApiResponse<DispatchDto>> create(@Valid @RequestBody CreateDispatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dispatchService.create(request), "Dispatch created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update dispatch")
    public ResponseEntity<ApiResponse<DispatchDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDispatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.update(id, request), "Dispatch updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update dispatch status")
    public ResponseEntity<ApiResponse<DispatchDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        DispatchStatus status = DispatchStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(dispatchService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/pod")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record proof of delivery")
    public ResponseEntity<ApiResponse<DispatchDto>> recordPOD(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        LocalDate podDate = body.get("podDate") != null ? LocalDate.parse(body.get("podDate")) : LocalDate.now();
        String podReference = body.get("podReference");
        return ResponseEntity.ok(ApiResponse.success(dispatchService.recordPOD(id, podDate, podReference), "POD recorded successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete dispatch (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        dispatchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dispatch deleted successfully"));
    }
}
