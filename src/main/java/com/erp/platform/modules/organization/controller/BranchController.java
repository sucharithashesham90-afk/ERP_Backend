package com.erp.platform.modules.organization.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.organization.dto.BranchDto;
import com.erp.platform.modules.organization.dto.CreateBranchRequest;
import com.erp.platform.modules.organization.service.BranchService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org/branches")
@RequiredArgsConstructor
@Tag(name = "Organization - Branches", description = "Branch management")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List branches")
    public ResponseEntity<ApiResponse<PageResponse<BranchDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID companyId) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(branchService.list(companyId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a branch")
    public ResponseEntity<ApiResponse<BranchDto>> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(branchService.create(request), "Branch created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(branchService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete branch (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        branchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted successfully"));
    }

    /**
     * Report duplicate branches, and fold them together when asked.
     *
     * <p>Defaults to reporting only: these are records other things point at, so the list should be
     * read before anything is removed.
     */
    @PostMapping("/deduplicate")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Report duplicate branches, or fold them together with apply=true")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deduplicate(
            @RequestParam(defaultValue = "false") boolean apply) {
        Map<String, Object> result = branchService.deduplicate(apply);
        return ResponseEntity.ok(ApiResponse.success(result,
                apply ? "Duplicate branches folded together" : "Preview only — nothing was changed"));
    }
}
