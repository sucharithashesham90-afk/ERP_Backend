package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.CreateLoginTrackingRequest;
import com.erp.platform.modules.admin.dto.LoginTrackingDto;
import com.erp.platform.modules.admin.service.LoginTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/login-tracking")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LoginTrackingController {

    private final LoginTrackingService loginTrackingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoginTrackingDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(loginTrackingService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoginTrackingDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(loginTrackingService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoginTrackingDto>> create(@Valid @RequestBody CreateLoginTrackingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loginTrackingService.create(request)));
    }

    @PostMapping("/{id}/force-logoff")
    public ResponseEntity<ApiResponse<LoginTrackingDto>> forceLogoff(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "admin") String adminUsername) {
        return ResponseEntity.ok(ApiResponse.success(loginTrackingService.forceLogoff(id, adminUsername)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        loginTrackingService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
