package com.erp.platform.modules.auth.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.auth.dto.UserGroupDto;
import com.erp.platform.modules.auth.dto.UserGroupRequest;
import com.erp.platform.modules.auth.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/groups")
@RequiredArgsConstructor
@Tag(name = "User Groups", description = "User group management — admin only")
public class UserGroupController {

    private final UserGroupService groupService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all user groups for this tenant")
    public ResponseEntity<ApiResponse<List<UserGroupDto>>> list() {
        return ResponseEntity.ok(ApiResponse.success(groupService.listAll()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a user group")
    public ResponseEntity<ApiResponse<UserGroupDto>> create(@Valid @RequestBody UserGroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(groupService.create(req), "Group created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a user group")
    public ResponseEntity<ApiResponse<UserGroupDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserGroupRequest req) {
        return ResponseEntity.ok(ApiResponse.success(groupService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a user group (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        groupService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Group deleted"));
    }
}
