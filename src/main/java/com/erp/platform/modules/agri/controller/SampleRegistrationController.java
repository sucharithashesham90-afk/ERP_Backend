package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSampleRegistrationRequest;
import com.erp.platform.modules.agri.dto.SampleRegistrationDto;
import com.erp.platform.modules.agri.service.SampleRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/sample-registrations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SampleRegistrationController {

    private final SampleRegistrationService sampleRegistrationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SampleRegistrationDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(sampleRegistrationService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SampleRegistrationDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sampleRegistrationService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SampleRegistrationDto>> create(@Valid @RequestBody CreateSampleRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sampleRegistrationService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SampleRegistrationDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSampleRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sampleRegistrationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sampleRegistrationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/create-batch")
    public ResponseEntity<ApiResponse<List<SampleRegistrationDto>>> createBatch(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(ApiResponse.success(sampleRegistrationService.createBatch(ids)));
    }
}
