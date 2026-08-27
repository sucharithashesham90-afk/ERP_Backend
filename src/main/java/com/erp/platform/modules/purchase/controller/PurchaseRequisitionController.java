package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePurchaseRequisitionRequest;
import com.erp.platform.modules.purchase.dto.PurchaseRequisitionDto;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition.ReqStatus;
import com.erp.platform.modules.purchase.service.PurchaseRequisitionService;
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
@RequestMapping("/api/v1/purchase/requisitions")
@RequiredArgsConstructor
@Tag(name = "Purchase - Requisitions", description = "Purchase requisition management")
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService requisitionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase requisitions")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseRequisitionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReqStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(requisitionService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get purchase requisition by ID")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(requisitionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> create(
            @RequestBody CreatePurchaseRequisitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(requisitionService.create(request), "Purchase requisition created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> update(
            @PathVariable UUID id,
            @RequestBody CreatePurchaseRequisitionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(requisitionService.update(id, request)));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit purchase requisition for approval")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> submit(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(requisitionService.submit(id), "Purchase requisition submitted"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> approve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String approver = body.get("approver");
        return ResponseEntity.ok(ApiResponse.success(requisitionService.approve(id, approver), "Purchase requisition approved"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject purchase requisition")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDto>> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(ApiResponse.success(requisitionService.reject(id, reason), "Purchase requisition rejected"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete purchase requisition (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        requisitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase requisition deleted successfully"));
    }
}
