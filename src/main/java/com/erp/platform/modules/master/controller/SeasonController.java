package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.entity.Season;
import com.erp.platform.modules.master.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master/seasons")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Season>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                seasonService.list(PageRequest.of(page, size, Sort.by("startDate").descending()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Season>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Season>> create(@RequestBody Season req) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Season>> update(@PathVariable UUID id, @RequestBody Season req) {
        return ResponseEntity.ok(ApiResponse.success(seasonService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        seasonService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
