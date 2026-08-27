package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateSubAccountRequest;
import com.erp.platform.modules.agri.dto.SubAccountDto;
import com.erp.platform.modules.agri.service.SubAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/sub-accounts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SubAccountController {

    private final SubAccountService subAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SubAccountDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(subAccountService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubAccountDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(subAccountService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubAccountDto>> create(@Valid @RequestBody CreateSubAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subAccountService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubAccountDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSubAccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subAccountService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        subAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
