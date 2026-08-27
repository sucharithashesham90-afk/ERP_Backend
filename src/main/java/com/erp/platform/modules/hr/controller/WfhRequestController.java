package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.WfhRequest;
import com.erp.platform.modules.hr.service.WfhRequestService;
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
@RequestMapping("/api/v1/hr/wfh-requests")
@RequiredArgsConstructor
@Tag(name = "HR - Work From Home", description = "Work-from-home requests with an approval workflow")
public class WfhRequestController {

    private final WfhRequestService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List WFH requests")
    public ResponseEntity<ApiResponse<PageResponse<WfhRequest>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Apply for work from home")
    public ResponseEntity<ApiResponse<WfhRequest>> apply(@RequestBody WfhRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.apply(req), "WFH request submitted"));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve a WFH request")
    public ResponseEntity<ApiResponse<WfhRequest>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id), "WFH approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject a WFH request")
    public ResponseEntity<ApiResponse<WfhRequest>> reject(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                service.reject(id, body != null ? body.get("reason") : null), "WFH rejected"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a WFH request")
    public ResponseEntity<ApiResponse<WfhRequest>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancel(id), "WFH cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a WFH request")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
