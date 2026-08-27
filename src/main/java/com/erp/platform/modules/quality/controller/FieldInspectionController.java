package com.erp.platform.modules.quality.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.quality.dto.FieldInspectionDto;
import com.erp.platform.modules.quality.service.FieldInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quality/field-inspections")
@RequiredArgsConstructor
public class FieldInspectionController {

    private final FieldInspectionService service;

    @GetMapping
    public ApiResponse<PageResponse<FieldInspectionDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}")
    public ApiResponse<FieldInspectionDto> getById(@PathVariable UUID id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    public ApiResponse<FieldInspectionDto> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(body));
    }

    @PutMapping("/{id}")
    public ApiResponse<FieldInspectionDto> updateHeader(@PathVariable UUID id,
                                                        @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.updateHeader(id, body));
    }

    @PutMapping("/{id}/rounds/{roundNumber}")
    public ApiResponse<FieldInspectionDto> saveRound(@PathVariable UUID id,
                                                     @PathVariable int roundNumber,
                                                     @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.saveRound(id, roundNumber, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
