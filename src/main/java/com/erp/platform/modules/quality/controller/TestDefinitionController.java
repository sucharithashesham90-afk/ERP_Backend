package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.CreateTestDefinitionRequest;
import com.erp.platform.modules.quality.dto.TestDefinitionDto;
import com.erp.platform.modules.quality.service.TestDefinitionService;
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
@RequestMapping("/api/v1/quality/test-definitions")
@RequiredArgsConstructor
@Tag(name = "Quality - Test Definitions", description = "Test definition management")
public class TestDefinitionController {

    private final TestDefinitionService testDefinitionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List test definitions")
    public ResponseEntity<ApiResponse<PageResponse<TestDefinitionDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(ApiResponse.success(testDefinitionService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get test definition by ID")
    public ResponseEntity<ApiResponse<TestDefinitionDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testDefinitionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create test definition")
    public ResponseEntity<ApiResponse<TestDefinitionDto>> create(@Valid @RequestBody CreateTestDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(testDefinitionService.create(request), "Test definition created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update test definition")
    public ResponseEntity<ApiResponse<TestDefinitionDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateTestDefinitionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(testDefinitionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete test definition (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        testDefinitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Test definition deleted successfully"));
    }
}
