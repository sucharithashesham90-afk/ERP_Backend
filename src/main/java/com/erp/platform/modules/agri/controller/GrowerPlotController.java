package com.erp.platform.modules.agri.controller;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateGrowerPlotRequest;
import com.erp.platform.modules.agri.dto.GrowerPlotDto;
import com.erp.platform.modules.agri.service.GrowerPlotService;
import com.erp.platform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agri/grower-plots")
@RequiredArgsConstructor
public class GrowerPlotController {

    private final GrowerPlotService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.list(page, size)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@RequestBody CreateGrowerPlotRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestBody CreateGrowerPlotRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
