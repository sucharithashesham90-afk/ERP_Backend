package com.erp.platform.modules.crm.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.crm.entity.Lead;
import com.erp.platform.modules.crm.entity.Lead.LeadStatus;
import com.erp.platform.modules.crm.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crm/leads")
@RequiredArgsConstructor
@Tag(name = "CRM - Leads", description = "Lead management")
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List leads")
    public ResponseEntity<ApiResponse<PageResponse<Lead>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) String search) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(leadService.list(status, search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get lead by ID")
    public ResponseEntity<ApiResponse<Lead>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create lead")
    public ResponseEntity<ApiResponse<Lead>> create(@RequestBody Lead lead) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leadService.create(lead), "Lead created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update lead")
    public ResponseEntity<ApiResponse<Lead>> update(@PathVariable UUID id, @RequestBody Lead body) {
        return ResponseEntity.ok(ApiResponse.success(leadService.update(id, body)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update lead status")
    public ResponseEntity<ApiResponse<Lead>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        LeadStatus status = LeadStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(leadService.updateStatus(id, status)));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get lead status counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> stats() {
        return ResponseEntity.ok(ApiResponse.success(leadService.getStatusCounts()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete lead (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leadService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
    }
}
