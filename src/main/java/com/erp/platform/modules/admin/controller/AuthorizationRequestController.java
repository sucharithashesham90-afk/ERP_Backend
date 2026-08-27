package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.AuthorizationRequestDto;
import com.erp.platform.modules.admin.dto.CreateAuthorizationRequestRequest;
import com.erp.platform.modules.admin.service.AuthorizationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/authorization-requests")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AuthorizationRequestController {

    private final AuthorizationRequestService authorizationRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuthorizationRequestDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(authorizationRequestService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorizationRequestDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(authorizationRequestService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuthorizationRequestDto>> create(@Valid @RequestBody CreateAuthorizationRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorizationRequestService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuthorizationRequestDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAuthorizationRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authorizationRequestService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        authorizationRequestService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
