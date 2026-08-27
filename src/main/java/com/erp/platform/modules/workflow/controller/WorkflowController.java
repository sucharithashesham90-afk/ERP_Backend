package com.erp.platform.modules.workflow.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.workflow.entity.WorkflowDefinition;
import com.erp.platform.modules.workflow.entity.WorkflowInstance;
import com.erp.platform.modules.workflow.entity.WorkflowInstance.WorkflowStatus;
import com.erp.platform.modules.workflow.service.WorkflowService;
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
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
@Tag(name = "Workflow", description = "Workflow and approval management")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping("/definitions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List workflow definitions")
    public ResponseEntity<ApiResponse<PageResponse<WorkflowDefinition>>> listDefinitions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String module) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(workflowService.listDefinitions(module, pageable)));
    }

    @PostMapping("/definitions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create workflow definition")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> createDefinition(@RequestBody WorkflowDefinition def) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workflowService.createDefinition(def), "Workflow definition created"));
    }

    @GetMapping("/instances")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List workflow instances")
    public ResponseEntity<ApiResponse<PageResponse<WorkflowInstance>>> listInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) WorkflowStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(workflowService.listInstances(status, pageable)));
    }

    @PostMapping("/instances/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Process workflow approval")
    public ResponseEntity<ApiResponse<WorkflowInstance>> processApproval(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        UUID approverId = UUID.fromString(body.get("approverId").toString());
        String action = body.get("action").toString();
        String comments = (String) body.getOrDefault("comments", "");
        return ResponseEntity.ok(ApiResponse.success(
                workflowService.processApproval(id, approverId, action, comments), "Processed"));
    }
}
