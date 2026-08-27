package com.erp.platform.modules.shareholder.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.shareholder.dto.ShareTransferDto;
import com.erp.platform.modules.shareholder.service.ShareTransferService;
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
@RequestMapping("/api/v1/shareholder/transfers")
@RequiredArgsConstructor
@Tag(name = "Share Transfers", description = "Share Capital — Transfer and transmission management")
public class ShareTransferController {

    private final ShareTransferService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List share transfers")
    public ResponseEntity<ApiResponse<PageResponse<ShareTransferDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                service.list(PageRequest.of(page, size, Sort.by("transferDate").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get share transfer by ID")
    public ResponseEntity<ApiResponse<ShareTransferDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create share transfer")
    public ResponseEntity<ApiResponse<ShareTransferDto>> create(@RequestBody ShareTransferDto req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Share transfer created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update share transfer")
    public ResponseEntity<ApiResponse<ShareTransferDto>> update(@PathVariable UUID id,
            @RequestBody ShareTransferDto req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve share transfer — updates sharesHeld on both shareholders")
    public ResponseEntity<ApiResponse<ShareTransferDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id), "Share transfer approved"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete share transfer")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Share transfer deleted"));
    }
}
