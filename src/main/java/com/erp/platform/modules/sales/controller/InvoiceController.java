package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateInvoiceRequest;
import com.erp.platform.modules.sales.dto.InvoiceDto;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import com.erp.platform.modules.sales.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sales/invoices")
@RequiredArgsConstructor
@Tag(name = "Sales - Invoices", description = "Invoice management")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final com.erp.platform.modules.sales.service.SalesInvoicePostingService postingService;
    private final com.erp.platform.modules.sales.service.InvoicePostingBackfillService backfillService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List invoices")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InvoiceStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(invoiceService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoice by ID")
    public ResponseEntity<ApiResponse<InvoiceDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create invoice")
    public ResponseEntity<ApiResponse<InvoiceDto>> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(invoiceService.create(request), "Invoice created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update invoice")
    public ResponseEntity<ApiResponse<InvoiceDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update invoice status")
    public ResponseEntity<ApiResponse<InvoiceDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        InvoiceStatus status = InvoiceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(invoiceService.updateStatus(id, status)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Complete the invoice and post the total to the customer ledger")
    public ResponseEntity<ApiResponse<InvoiceDto>> complete(@PathVariable UUID id) {
        postingService.completeInvoice(id);
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getById(id), "Invoice completed and posted to ledgers"));
    }

    @PostMapping("/backfill-postings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Post all previously-unposted invoices to the ledger (one-off backfill)")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> backfillPostings() {
        return ResponseEntity.ok(ApiResponse.success(backfillService.backfillUnposted(),
                "Backfilled previously-unposted invoices to the ledger"));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record payment against invoice with voucher and cheque tracking")
    public ResponseEntity<ApiResponse<InvoiceDto>> recordPayment(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = (String) body.getOrDefault("paymentMethod", "BANK_TRANSFER");
        String ref = (String) body.getOrDefault("reference", "");
        String chequeNo = (String) body.getOrDefault("chequeNumber", body.getOrDefault("chequeNo", ref));
        String chequeDate = (String) body.getOrDefault("chequeDate", null);
        return ResponseEntity.ok(ApiResponse.success(
                invoiceService.recordPayment(id, amount, method, ref, chequeNo, chequeDate), "Payment and voucher entry recorded successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete invoice (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted successfully"));
    }

    @GetMapping(value = "/{id}/print", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get printable HTML for a single invoice")
    public ResponseEntity<String> print(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.generateInvoiceHtml(id));
    }

    @GetMapping(value = "/batch-print", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get combined printable HTML for multiple invoices (comma-separated ids)")
    public ResponseEntity<String> batchPrint(@RequestParam String ids) {
        List<UUID> idList = Arrays.stream(ids.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceService.generateBatchHtml(idList));
    }
}
