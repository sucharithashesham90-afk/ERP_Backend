package com.erp.platform.modules.workflow.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.workflow.dto.ApprovalActionRequest;
import com.erp.platform.modules.workflow.dto.ApprovalInstanceDto;
import com.erp.platform.modules.workflow.entity.ApprovalInstance.ApprovalStatus;
import com.erp.platform.modules.workflow.service.ApprovalInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow/approvals")
@RequiredArgsConstructor
@Tag(name = "Workflow - Approvals", description = "Approval instance management")
public class ApprovalInstanceController {

    private final ApprovalInstanceService approvalInstanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List approval instances")
    public ResponseEntity<ApiResponse<PageResponse<ApprovalInstanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) ApprovalStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        return ResponseEntity.ok(ApiResponse.success(approvalInstanceService.list(documentType, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get approval instance by ID")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(approvalInstanceService.getById(id)));
    }

    @GetMapping("/by-document")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get approval instance by document")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> getByDocument(
            @RequestParam String documentType,
            @RequestParam UUID documentId) {
        return ResponseEntity.ok(ApiResponse.success(approvalInstanceService.getByDocument(documentType, documentId)));
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit document for approval")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> submit(@RequestBody Map<String, Object> body) {
        String documentType = (String) body.get("documentType");
        UUID documentId = UUID.fromString((String) body.get("documentId"));
        String documentNumber = (String) body.get("documentNumber");
        String submittedBy = (String) body.get("submittedBy");
        int maxLevel = body.containsKey("maxLevel") ? Integer.parseInt(body.get("maxLevel").toString()) : 1;
        return ResponseEntity.ok(ApiResponse.success(
                approvalInstanceService.submit(documentType, documentId, documentNumber, submittedBy, maxLevel),
                "Document submitted for approval"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve current level")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> approve(
            @PathVariable UUID id,
            @RequestBody ApprovalActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalInstanceService.approve(id, request.getAssignedTo(), request.getComments()),
                "Approved successfully"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject current level")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> reject(
            @PathVariable UUID id,
            @RequestBody ApprovalActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalInstanceService.reject(id, request.getAssignedTo(), request.getComments()),
                "Rejected successfully"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel approval instance")
    public ResponseEntity<ApiResponse<ApprovalInstanceDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(approvalInstanceService.cancel(id), "Approval cancelled"));
    }
}
