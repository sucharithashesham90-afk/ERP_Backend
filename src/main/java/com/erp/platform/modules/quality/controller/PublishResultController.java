package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.CreatePublishResultRequest;
import com.erp.platform.modules.quality.dto.PublishResultDto;
import com.erp.platform.modules.quality.service.PublishResultService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quality/publish-results")
@RequiredArgsConstructor
@Tag(name = "Publish Results", description = "Manage publish results")
public class PublishResultController {

    private final PublishResultService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List publish results")
    public ResponseEntity<ApiResponse<PageResponse<PublishResultDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("publishNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PublishResultDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PublishResultDto>> create(@Valid @RequestBody CreatePublishResultRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "publishResult created"));
    }

    @PostMapping("/publish-samples")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk-publish selected graded samples from Result Entry")
    public ResponseEntity<ApiResponse<List<PublishResultDto>>> publishSamples(@RequestBody Map<String, List<UUID>> body) {
        List<UUID> sampleIds = body.getOrDefault("sampleIds", List.of());
        return ResponseEntity.ok(ApiResponse.success(service.publishSamples(sampleIds), "Results published"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PublishResultDto>> update(@PathVariable UUID id, @Valid @RequestBody CreatePublishResultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "publishResult deleted"));
    }
}
