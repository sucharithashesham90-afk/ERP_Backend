package com.erp.platform.modules.admin.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.admin.dto.CreateServerConfigRequest;
import com.erp.platform.modules.admin.dto.ServerConfigDto;
import com.erp.platform.modules.admin.service.ServerConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/server-config")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ServerConfigController {

    private final ServerConfigService serverConfigService;

    /** Structured settings for one category (e.g. SMTP or SMS) as a key→value map. */
    @GetMapping("/by-category/{category}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.getByCategory(category)));
    }

    /** Upsert all settings for a category in one call (used by the SMTP / SMS forms). */
    @PutMapping("/by-category/{category}")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveByCategory(
            @PathVariable String category, @RequestBody Map<String, String> values) {
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.saveByCategory(category, values), "Saved"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServerConfigDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerConfigDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServerConfigDto>> create(@Valid @RequestBody CreateServerConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServerConfigDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateServerConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(serverConfigService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        serverConfigService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
