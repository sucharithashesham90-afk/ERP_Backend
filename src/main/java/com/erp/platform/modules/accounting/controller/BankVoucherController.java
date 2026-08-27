package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.BankVoucherDto;
import com.erp.platform.modules.accounting.dto.CreateBankVoucherRequest;
import com.erp.platform.modules.accounting.entity.BankVoucher;
import com.erp.platform.modules.accounting.service.BankVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/bank-vouchers")
@RequiredArgsConstructor
@Tag(name = "Accounting - Bank Vouchers", description = "Bank Receipt and Bank Payment voucher entry")
public class BankVoucherController {

    private final BankVoucherService bankVoucherService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bank vouchers with optional filters: type, status, dateFrom, dateTo, paymentMode")
    public ResponseEntity<ApiResponse<PageResponse<BankVoucherDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String paymentMode) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "voucherDate").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        BankVoucher.VoucherType typeEnum = (type != null && !type.isBlank())
                ? BankVoucher.VoucherType.valueOf(type) : null;
        LocalDate from = (dateFrom != null && !dateFrom.isBlank()) ? LocalDate.parse(dateFrom) : null;
        LocalDate to   = (dateTo   != null && !dateTo.isBlank())   ? LocalDate.parse(dateTo)   : null;
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.list(typeEnum, status, from, to, paymentMode, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get bank voucher by ID")
    public ResponseEntity<ApiResponse<BankVoucherDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create bank voucher (RECEIPT or PAYMENT). Set postImmediately=true to post to GL immediately.")
    public ResponseEntity<ApiResponse<BankVoucherDto>> create(@RequestBody CreateBankVoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(bankVoucherService.create(request), "Bank voucher created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update bank voucher (DRAFT only)")
    public ResponseEntity<ApiResponse<BankVoucherDto>> update(
            @PathVariable UUID id,
            @RequestBody CreateBankVoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.update(id, request)));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post bank voucher to GL (creates journal entry)")
    public ResponseEntity<ApiResponse<BankVoucherDto>> post(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.post(id), "Voucher posted to GL successfully"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a bank voucher")
    public ResponseEntity<ApiResponse<BankVoucherDto>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.cancel(id), "Voucher cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete bank voucher (DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        bankVoucherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/outstanding")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recent journal entries for a ledger account — used for Payment Invoice Popup")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> outstanding(
            @RequestParam UUID accountId) {
        return ResponseEntity.ok(ApiResponse.success(bankVoucherService.getOutstandingEntries(accountId)));
    }

    @GetMapping(value = "/{id}/payment-advice", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Payment Advice HTML for printing")
    public ResponseEntity<String> paymentAdvice(@PathVariable UUID id) {
        return ResponseEntity.ok(bankVoucherService.generatePaymentAdviceHtml(id));
    }

    @GetMapping(value = "/{id}/cheque", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Cheque HTML for printing")
    public ResponseEntity<String> cheque(@PathVariable UUID id) {
        return ResponseEntity.ok(bankVoucherService.generateChequeHtml(id));
    }

    @GetMapping(value = "/batch-print", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get combined payment advice HTML for multiple vouchers (comma-separated ids)")
    public ResponseEntity<String> batchPrint(@RequestParam String ids) {
        List<UUID> idList = Arrays.stream(ids.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bankVoucherService.generateBatchHtml(idList));
    }
}
