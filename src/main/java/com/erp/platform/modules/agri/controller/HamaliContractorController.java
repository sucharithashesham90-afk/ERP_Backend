package com.erp.platform.modules.agri.controller;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateHamaliContractorRequest;
import com.erp.platform.modules.agri.dto.HamaliContractorDto;
import com.erp.platform.modules.agri.service.HamaliContractorService;
import com.erp.platform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agri/hamali-contractors")
@RequiredArgsConstructor
public class HamaliContractorController {

    private final HamaliContractorService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(page, size)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@RequestBody CreateHamaliContractorRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestBody CreateHamaliContractorRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
