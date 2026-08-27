package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.NetDto;
import com.erp.platform.modules.inventory.service.NetService;
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
@RequestMapping("/api/v1/inventory/nets")
@RequiredArgsConstructor
@Tag(name = "Inventory - Nets", description = "Net (bin) master inside godowns")
public class NetController {

    private final NetService netService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List nets")
    public ResponseEntity<ApiResponse<PageResponse<NetDto>>> list(
            @RequestParam(required = false) UUID godownId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(netService.list(godownId, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create net")
    public ResponseEntity<ApiResponse<NetDto>> create(@RequestBody NetDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(netService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update net")
    public ResponseEntity<ApiResponse<NetDto>> update(@PathVariable UUID id, @RequestBody NetDto request) {
        return ResponseEntity.ok(ApiResponse.success(netService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete net")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        netService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
