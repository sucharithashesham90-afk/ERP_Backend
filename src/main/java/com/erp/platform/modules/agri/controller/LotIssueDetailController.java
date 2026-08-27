package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateLotIssueDetailRequest;
import com.erp.platform.modules.agri.dto.LotIssueDetailDto;
import com.erp.platform.modules.agri.service.LotIssueDetailService;
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
@RequestMapping("/api/v1/agri/lot-issue-details")
@RequiredArgsConstructor
@Tag(name = "Lot Issue Details", description = "Manage lot issue details")
public class LotIssueDetailController {

    private final LotIssueDetailService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List lot issue details")
    public ResponseEntity<ApiResponse<PageResponse<LotIssueDetailDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("issueNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotIssueDetailDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotIssueDetailDto>> create(@Valid @RequestBody CreateLotIssueDetailRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "lotIssueDetail created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LotIssueDetailDto>> update(@PathVariable UUID id, @Valid @RequestBody CreateLotIssueDetailRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "lotIssueDetail deleted"));
    }
}
