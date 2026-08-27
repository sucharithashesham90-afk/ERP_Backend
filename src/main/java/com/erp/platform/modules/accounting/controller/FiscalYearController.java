package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.FiscalYearDto;
import com.erp.platform.modules.accounting.service.FiscalYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/fiscal-years")
@RequiredArgsConstructor
@Tag(name = "Accounting - Fiscal Years", description = "Period definition and fiscal year management")
public class FiscalYearController {

    private final FiscalYearService fiscalYearService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List fiscal years (paged)")
    public ResponseEntity<ApiResponse<PageResponse<FiscalYearDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        return ResponseEntity.ok(ApiResponse.success(fiscalYearService.list(pageable)));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all fiscal years (unpaged)")
    public ResponseEntity<ApiResponse<List<FiscalYearDto>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(fiscalYearService.listAll()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new fiscal year / period")
    public ResponseEntity<ApiResponse<FiscalYearDto>> create(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(fiscalYearService.create(request)));
    }

    @PostMapping("/generate-next")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate the next yearly period (Apr 1 – Mar 31) from the latest record")
    public ResponseEntity<ApiResponse<FiscalYearDto>> generateNext() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fiscalYearService.generateNext(), "Next period generated"));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'ACCOUNT_MANAGER')")
    @Operation(summary = "Close a fiscal year")
    public ResponseEntity<ApiResponse<FiscalYearDto>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(fiscalYearService.close(id), "Fiscal year closed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete an INITIALIZED fiscal year")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        fiscalYearService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
