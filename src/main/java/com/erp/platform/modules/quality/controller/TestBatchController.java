package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.CreateTestBatchRequest;
import com.erp.platform.modules.quality.dto.TestBatchDto;
import com.erp.platform.modules.quality.entity.TestBatch.TestStatus;
import com.erp.platform.modules.quality.service.TestBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quality/test-batches")
@RequiredArgsConstructor
@Tag(name = "Quality - Test Batches", description = "Test batch management")
public class TestBatchController {

    private final TestBatchService testBatchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List test batches")
    public ResponseEntity<ApiResponse<PageResponse<TestBatchDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TestStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(testBatchService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get test batch by ID")
    public ResponseEntity<ApiResponse<TestBatchDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testBatchService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create test batch")
    public ResponseEntity<ApiResponse<TestBatchDto>> create(@RequestBody CreateTestBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(testBatchService.create(request), "Test batch created successfully"));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete test batch and compute overall result")
    public ResponseEntity<ApiResponse<TestBatchDto>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(testBatchService.complete(id), "Test batch completed"));
    }

    @PutMapping("/{id}/results")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update test result details")
    public ResponseEntity<ApiResponse<TestBatchDto>> updateResults(
            @PathVariable UUID id,
            @RequestBody List<CreateTestBatchRequest.ResultDetailRequest> results) {
        return ResponseEntity.ok(ApiResponse.success(testBatchService.updateResults(id, results)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete test batch (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        testBatchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Test batch deleted successfully"));
    }
}
