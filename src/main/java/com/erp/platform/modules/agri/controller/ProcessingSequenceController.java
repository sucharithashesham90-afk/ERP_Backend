package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessingSequenceRequest;
import com.erp.platform.modules.agri.dto.ProcessingSequenceDto;
import com.erp.platform.modules.agri.service.ProcessingSequenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/processing-sequences")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProcessingSequenceController {

    private final ProcessingSequenceService processingSequenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProcessingSequenceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(processingSequenceService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessingSequenceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(processingSequenceService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessingSequenceDto>> create(@Valid @RequestBody CreateProcessingSequenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processingSequenceService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessingSequenceDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProcessingSequenceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processingSequenceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        processingSequenceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
