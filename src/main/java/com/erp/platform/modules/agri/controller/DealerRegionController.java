package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateDealerRegionRequest;
import com.erp.platform.modules.agri.dto.DealerRegionDto;
import com.erp.platform.modules.agri.service.DealerRegionService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/dealer-regions")
@RequiredArgsConstructor
@Tag(name = "Dealer Regions", description = "Sales territory/region management")
public class DealerRegionController {

    private final DealerRegionService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List dealer regions")
    public ResponseEntity<ApiResponse<PageResponse<DealerRegionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get dealer region by ID")
    public ResponseEntity<ApiResponse<DealerRegionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create dealer region")
    public ResponseEntity<ApiResponse<DealerRegionDto>> create(@Valid @RequestBody CreateDealerRegionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Dealer region created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update dealer region")
    public ResponseEntity<ApiResponse<DealerRegionDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateDealerRegionRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete dealer region")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dealer region deleted"));
    }
}
