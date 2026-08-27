package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.CreateOpeningBalanceRequest;
import com.erp.platform.modules.accounting.dto.MigrationBatchDto;
import com.erp.platform.modules.accounting.dto.OpeningBalanceDto;
import com.erp.platform.modules.accounting.entity.OpeningBalance.BalanceType;
import com.erp.platform.modules.accounting.entity.OpeningBalance.MigrationStatus;
import com.erp.platform.modules.accounting.service.OpeningBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/opening-balances")
@RequiredArgsConstructor
@Tag(name = "Opening Balances", description = "Opening balance migration management")
public class OpeningBalanceController {

    private final OpeningBalanceService openingBalanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List opening balances")
    public ResponseEntity<ApiResponse<PageResponse<OpeningBalanceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) BalanceType balanceType,
            @RequestParam(required = false) MigrationStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(openingBalanceService.list(balanceType, status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get opening balance by ID")
    public ResponseEntity<ApiResponse<OpeningBalanceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(openingBalanceService.getById(id)));
    }

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create migration batch with balance lines")
    public ResponseEntity<ApiResponse<MigrationBatchDto>> createBatch(@RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        openingBalanceService.createBatch(request.getDescription(), request.getAsOfDate(), request.getLines()),
                        "Migration batch created successfully"));
    }

    @PostMapping("/batch/{batchNumber}/validate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Validate migration batch")
    public ResponseEntity<ApiResponse<MigrationBatchDto>> validateBatch(@PathVariable String batchNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                openingBalanceService.validateBatch(batchNumber), "Batch validated successfully"));
    }

    @PostMapping("/batch/{batchNumber}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post migration batch")
    public ResponseEntity<ApiResponse<MigrationBatchDto>> postBatch(@PathVariable String batchNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                openingBalanceService.postBatch(batchNumber), "Batch posted successfully"));
    }

    @PostMapping("/batch/{batchNumber}/reverse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reverse migration batch")
    public ResponseEntity<ApiResponse<MigrationBatchDto>> reverseBatch(@PathVariable String batchNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                openingBalanceService.reverseBatch(batchNumber), "Batch reversed successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete opening balance (soft delete, DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        openingBalanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Opening balance deleted successfully"));
    }

    @Data
    public static class CreateBatchRequest {
        private String description;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate asOfDate;
        private List<CreateOpeningBalanceRequest> lines;
    }
}
