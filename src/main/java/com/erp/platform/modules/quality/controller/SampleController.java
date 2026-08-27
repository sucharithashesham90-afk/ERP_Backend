package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.CreateSampleRequest;
import com.erp.platform.modules.quality.dto.SampleDto;
import com.erp.platform.modules.quality.entity.Sample.SampleStatus;
import com.erp.platform.modules.quality.service.SampleService;
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
@RequestMapping("/api/v1/quality/samples")
@RequiredArgsConstructor
@Tag(name = "Quality - Samples", description = "Sample management")
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List samples")
    public ResponseEntity<ApiResponse<PageResponse<SampleDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SampleStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(sampleService.list(status, pageable)));
    }

    /**
     * Lot Search. Reads recorded results off samples, which is where Result Entry writes them.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search recorded test results by lot, product, date range and result")
    public ResponseEntity<ApiResponse<PageResponse<java.util.Map<String, Object>>>> search(
            @RequestParam(required = false, defaultValue = "") String lot,
            @RequestParam(required = false, defaultValue = "") String product,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "") String result,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        java.time.LocalDate fromD = from == null || from.isBlank() ? null : java.time.LocalDate.parse(from);
        java.time.LocalDate toD = to == null || to.isBlank() ? null : java.time.LocalDate.parse(to);
        return ResponseEntity.ok(ApiResponse.success(sampleService.searchResults(
                lot, product, fromD, toD, result, PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sample by ID")
    public ResponseEntity<ApiResponse<SampleDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sampleService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create sample")
    public ResponseEntity<ApiResponse<SampleDto>> create(@RequestBody CreateSampleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sampleService.create(request), "Sample created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sample")
    public ResponseEntity<ApiResponse<SampleDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateSampleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sampleService.update(id, request)));
    }

    @PostMapping("/create-batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign one shared batch number to multiple selected samples")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBatch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        var ids = (java.util.List<String>) body.getOrDefault("sampleIds", java.util.List.of());
        var uuids = ids.stream().map(UUID::fromString).toList();
        String batchNumber = sampleService.createBatch(uuids);
        return ResponseEntity.ok(ApiResponse.success(Map.of("batchNumber", batchNumber), "Batch created"));
    }

    @PutMapping("/{id}/result")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Save Result Entry outcome (observed values + PASS/FAIL) for a sample")
    public ResponseEntity<ApiResponse<SampleDto>> saveResult(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String resultStatus = body.get("resultStatus") != null ? body.get("resultStatus").toString() : "PENDING";
        String resultsJson = body.get("resultsJson") != null ? body.get("resultsJson").toString() : null;
        return ResponseEntity.ok(ApiResponse.success(sampleService.saveResult(id, resultStatus, resultsJson)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update sample status")
    public ResponseEntity<ApiResponse<SampleDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        SampleStatus status = SampleStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(sampleService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete sample (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sampleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sample deleted successfully"));
    }
}
