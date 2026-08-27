package com.erp.platform.modules.hr.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreatePayGradeRequest;
import com.erp.platform.modules.hr.dto.PayGradeDto;
import com.erp.platform.modules.hr.service.PayGradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/pay-grades")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PayGradeController {

    private final PayGradeService payGradeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PayGradeDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(payGradeService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayGradeDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(payGradeService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PayGradeDto>> create(@Valid @RequestBody CreatePayGradeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(payGradeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PayGradeDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePayGradeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(payGradeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        payGradeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
