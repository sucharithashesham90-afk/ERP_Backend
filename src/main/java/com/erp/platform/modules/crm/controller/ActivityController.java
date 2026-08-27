package com.erp.platform.modules.crm.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.crm.entity.Activity;
import com.erp.platform.modules.crm.service.ActivityService;
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
@RequestMapping("/api/v1/crm/activities")
@RequiredArgsConstructor
@Tag(name = "CRM - Activities", description = "Activity tracking for leads and customers")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List activities")
    public ResponseEntity<ApiResponse<PageResponse<Activity>>> list(
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(activityService.list(referenceId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get activity by ID")
    public ResponseEntity<ApiResponse<Activity>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(activityService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create activity")
    public ResponseEntity<ApiResponse<Activity>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(activityService.create(body), "Activity created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update activity")
    public ResponseEntity<ApiResponse<Activity>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(activityService.update(id, body)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark activity as completed")
    public ResponseEntity<ApiResponse<Activity>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(activityService.complete(id), "Activity completed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete activity")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        activityService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
