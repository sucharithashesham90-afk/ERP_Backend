package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateViabilityTestRequest;
import com.erp.platform.modules.agri.dto.ViabilityTestDto;
import com.erp.platform.modules.agri.service.ViabilityTestService;
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
@RequestMapping("/api/v1/agri/viability-tests")
@RequiredArgsConstructor
@Tag(name = "Viability Tests", description = "Agricultural viability and quality testing")
public class ViabilityTestController {

    private final ViabilityTestService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List viability tests")
    public ResponseEntity<ApiResponse<PageResponse<ViabilityTestDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("testDate").descending());
        return ResponseEntity.ok(ApiResponse.success(service.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get viability test by ID")
    public ResponseEntity<ApiResponse<ViabilityTestDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create viability test")
    public ResponseEntity<ApiResponse<ViabilityTestDto>> create(@Valid @RequestBody CreateViabilityTestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(req), "Viability test created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update viability test")
    public ResponseEntity<ApiResponse<ViabilityTestDto>> update(@PathVariable UUID id,
            @Valid @RequestBody CreateViabilityTestRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete viability test")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Viability test deleted"));
    }
}
