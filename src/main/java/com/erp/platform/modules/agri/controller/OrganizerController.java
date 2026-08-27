package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateOrganizerRequest;
import com.erp.platform.modules.agri.dto.OrganizerDto;
import com.erp.platform.modules.agri.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/organizers")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrganizerController {

    private final OrganizerService organizerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrganizerDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(organizerService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(organizerService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizerDto>> create(@Valid @RequestBody CreateOrganizerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(organizerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizerDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrganizerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(organizerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        organizerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
