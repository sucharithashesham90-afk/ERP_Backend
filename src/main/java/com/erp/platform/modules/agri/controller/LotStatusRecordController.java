package com.erp.platform.modules.agri.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateLotStatusRecordRequest;
import com.erp.platform.modules.agri.dto.LotStatusRecordDto;
import com.erp.platform.modules.agri.service.LotStatusRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agri/lot-status")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LotStatusRecordController {

    private final LotStatusRecordService lotStatusRecordService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LotStatusRecordDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(lotStatusRecordService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LotStatusRecordDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(lotStatusRecordService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LotStatusRecordDto>> create(@Valid @RequestBody CreateLotStatusRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lotStatusRecordService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LotStatusRecordDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateLotStatusRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lotStatusRecordService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        lotStatusRecordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
