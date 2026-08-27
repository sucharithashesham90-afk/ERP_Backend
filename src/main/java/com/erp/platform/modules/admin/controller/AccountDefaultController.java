package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.AccountDefaultDto;
import com.erp.platform.modules.admin.dto.CreateAccountDefaultRequest;
import com.erp.platform.modules.admin.service.AccountDefaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/account-defaults")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AccountDefaultController {

    private final AccountDefaultService accountDefaultService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AccountDefaultDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(accountDefaultService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDefaultDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountDefaultService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDefaultDto>> create(@Valid @RequestBody CreateAccountDefaultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(accountDefaultService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDefaultDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAccountDefaultRequest request) {
        return ResponseEntity.ok(ApiResponse.success(accountDefaultService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        accountDefaultService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
