package com.erp.platform.modules.workflow.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.workflow.dto.ApprovalRuleDto;
import com.erp.platform.modules.workflow.dto.CreateApprovalRuleRequest;
import com.erp.platform.modules.workflow.service.ApprovalRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow/approval-rules")
@RequiredArgsConstructor
@Tag(name = "Workflow - Approval Rules", description = "Approval rule configuration")
public class ApprovalRuleController {

    private final ApprovalRuleService approvalRuleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List approval rules")
    public ResponseEntity<ApiResponse<PageResponse<ApprovalRuleDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String documentType) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "approvalLevel"));
        return ResponseEntity.ok(ApiResponse.success(approvalRuleService.list(documentType, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create approval rule")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> create(@RequestBody CreateApprovalRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(approvalRuleService.create(request), "Approval rule created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update approval rule")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateApprovalRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(approvalRuleService.update(id, request), "Approval rule updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete approval rule")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        approvalRuleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Approval rule deleted successfully"));
    }
}
