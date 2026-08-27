package com.erp.platform.modules.inventory.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.inventory.dto.GodownDto;
import com.erp.platform.modules.inventory.service.GodownService;
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
@RequestMapping("/api/v1/inventory/godowns")
@RequiredArgsConstructor
@Tag(name = "Inventory - Godowns", description = "Godown (storage building) master")
public class GodownController {

    private final GodownService godownService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List godowns")
    public ResponseEntity<ApiResponse<PageResponse<GodownDto>>> list(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(godownService.list(location, pageable)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create godown")
    public ResponseEntity<ApiResponse<GodownDto>> create(@RequestBody GodownDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(godownService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update godown")
    public ResponseEntity<ApiResponse<GodownDto>> update(@PathVariable UUID id, @RequestBody GodownDto request) {
        return ResponseEntity.ok(ApiResponse.success(godownService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete godown")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        godownService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
