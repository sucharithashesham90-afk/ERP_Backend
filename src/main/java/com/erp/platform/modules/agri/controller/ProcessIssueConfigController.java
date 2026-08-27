package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateProcessIssueConfigRequest;
import com.erp.platform.modules.agri.dto.ProcessIssueConfigDto;
import com.erp.platform.modules.agri.service.ProcessIssueConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/process-issue-configs")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProcessIssueConfigController {

    private final ProcessIssueConfigService processIssueConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProcessIssueConfigDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(processIssueConfigService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessIssueConfigDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(processIssueConfigService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProcessIssueConfigDto>> create(@Valid @RequestBody CreateProcessIssueConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processIssueConfigService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcessIssueConfigDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProcessIssueConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(processIssueConfigService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        processIssueConfigService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
