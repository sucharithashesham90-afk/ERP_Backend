package com.erp.platform.modules.manufacturing.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.manufacturing.entity.QualityRejection;
import com.erp.platform.modules.manufacturing.entity.QualityRejection.RejectionStatus;
import com.erp.platform.modules.manufacturing.service.QualityRejectionService;
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
@RequestMapping("/api/v1/manufacturing/quality-rejections")
@RequiredArgsConstructor
@Tag(name = "Manufacturing - Quality Rejections", description = "Quality rejection tracking")
public class QualityRejectionController {

    private final QualityRejectionService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List quality rejections")
    public ResponseEntity<ApiResponse<PageResponse<QualityRejection>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) RejectionStatus status,
            @RequestParam(required = false) UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(service.list(status, productId, PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get quality rejection by ID")
    public ResponseEntity<ApiResponse<QualityRejection>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create quality rejection")
    public ResponseEntity<ApiResponse<QualityRejection>> create(@RequestBody QualityRejection req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quality rejection")
    public ResponseEntity<ApiResponse<QualityRejection>> update(@PathVariable UUID id, @RequestBody QualityRejection req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @PatchMapping("/{id}/disposition")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update disposition for rejection")
    public ResponseEntity<ApiResponse<QualityRejection>> updateDisposition(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(service.updateDisposition(id, body.get("disposition"), body.get("notes"))));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resolve quality rejection with CA/PA")
    public ResponseEntity<ApiResponse<QualityRejection>> resolve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(service.resolve(id, body.get("correctiveAction"), body.get("preventiveAction"))));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close quality rejection")
    public ResponseEntity<ApiResponse<QualityRejection>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.close(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete quality rejection")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
