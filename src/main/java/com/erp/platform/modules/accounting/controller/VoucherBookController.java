package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.VoucherBook;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.repository.VoucherBookRepository;
import com.erp.platform.modules.accounting.service.VoucherBookService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/voucher-books")
@RequiredArgsConstructor
@Tag(name = "Accounting - Voucher Books", description = "Voucher book and numbering series definition")
public class VoucherBookController {

    private final VoucherBookRepository repo;
    private final JournalEntryRepository journalEntryRepository;
    private final VoucherBookService voucherBookService;
    private final TenantContext tenantContext;

    @GetMapping("/document-mapping")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Which voucher book each document type posts into")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> documentMapping() {
        return ResponseEntity.ok(ApiResponse.success(voucherBookService.documentMapping()));
    }

    @PostMapping("/backfill-journal-entries")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Classify existing vouchers into their books (numbers already issued are kept)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> backfill() {
        return ResponseEntity.ok(ApiResponse.success(
                voucherBookService.backfillJournalEntries(tenantContext.current()),
                "Existing vouchers classified into their books"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List voucher books")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId,
                        PageRequest.of(page, size, Sort.by("name"))).map(this::toMap))));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List active voucher books")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listActive() {
        UUID tenantId = tenantContext.current();
        return ResponseEntity.ok(ApiResponse.success(
                repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId)
                        .stream().map(this::toMap).collect(Collectors.toList())));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create voucher book")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        String code = (String) req.get("code");
        if (repo.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code))
            throw AppException.badRequest("Voucher book code already exists: " + code);
        VoucherBook vb = new VoucherBook();
        vb.setTenantId(tenantId);
        vb.setCode(code != null && !code.trim().isEmpty() ? code : (String) req.getOrDefault("abbreviation", "VB"));
        vb.setName((String) req.get("name"));
        vb.setVoucherType((String) req.get("voucherType"));
        vb.setPrefix((String) req.get("prefix"));
        vb.setSuffix((String) req.get("suffix"));
        vb.setStartNumber(Integer.parseInt(req.getOrDefault("startNumber", "1").toString()));
        vb.setCurrentNumber(Integer.parseInt(req.getOrDefault("currentNumber", "1").toString()));
        vb.setDescription((String) req.get("description"));
        vb.setActive(Boolean.parseBoolean(req.getOrDefault("active", "true").toString()));
        vb.setAbbreviation((String) req.getOrDefault("abbreviation", ""));
        vb.setPeriod((String) req.getOrDefault("period", "01 Apr 2026 - 31 Mar 2027"));
        vb.setAutoPosting(Boolean.parseBoolean(req.getOrDefault("autoPosting", "true").toString()));
        vb.setAutoPostToLedger(Boolean.parseBoolean(req.getOrDefault("autoPostToLedger", "false").toString()));
        vb.setGroupCodes((String) req.get("groupCodes"));
        vb.setAllowBackDateDays(Integer.parseInt(req.getOrDefault("allowBackDateDays", "0").toString()));
        vb.setVoucherPost(Boolean.parseBoolean(req.getOrDefault("voucherPost", "false").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(repo.save(vb))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update voucher book")
    public ResponseEntity<ApiResponse<Map<String, Object>>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        UUID tenantId = tenantContext.current();
        VoucherBook vb = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Voucher book not found: " + id));
        if (req.containsKey("name")) vb.setName((String) req.get("name"));
        if (req.containsKey("voucherType")) vb.setVoucherType((String) req.get("voucherType"));
        if (req.containsKey("prefix")) vb.setPrefix((String) req.get("prefix"));
        if (req.containsKey("suffix")) vb.setSuffix((String) req.get("suffix"));
        if (req.containsKey("startNumber")) vb.setStartNumber(Integer.parseInt(req.get("startNumber").toString()));
        if (req.containsKey("currentNumber")) vb.setCurrentNumber(Integer.parseInt(req.get("currentNumber").toString()));
        if (req.containsKey("description")) vb.setDescription((String) req.get("description"));
        if (req.containsKey("active")) vb.setActive(Boolean.parseBoolean(req.get("active").toString()));
        if (req.containsKey("abbreviation")) vb.setAbbreviation((String) req.get("abbreviation"));
        if (req.containsKey("period")) vb.setPeriod((String) req.get("period"));
        if (req.containsKey("autoPosting")) vb.setAutoPosting(Boolean.parseBoolean(req.get("autoPosting").toString()));
        if (req.containsKey("autoPostToLedger")) vb.setAutoPostToLedger(Boolean.parseBoolean(req.get("autoPostToLedger").toString()));
        if (req.containsKey("groupCodes")) vb.setGroupCodes((String) req.get("groupCodes"));
        if (req.containsKey("allowBackDateDays")) vb.setAllowBackDateDays(Integer.parseInt(req.get("allowBackDateDays").toString()));
        if (req.containsKey("voucherPost")) vb.setVoucherPost(Boolean.parseBoolean(req.get("voucherPost").toString()));
        return ResponseEntity.ok(ApiResponse.success(toMap(repo.save(vb))));
    }

    /**
     * Removes the book outright rather than flagging deleted_at, so its code is free to be reused —
     * a soft delete would keep occupying the code in the unique index on (tenant_id, code).
     *
     * Books that vouchers have already been numbered out of are kept: deleting one would orphan the
     * voucher_book_id those entries carry, and deactivating is the right way to retire a book.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete voucher book")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        VoucherBook vb = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Voucher book not found: " + id));
        if (journalEntryRepository.existsByTenantIdAndVoucherBookIdAndDeletedAtIsNull(tenantId, id))
            throw AppException.badRequest("Vouchers have already been posted into '" + vb.getName()
                    + "'. Deactivate the book instead of deleting it.");
        repo.delete(vb);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Map<String, Object> toMap(VoucherBook v) {
        java.util.LinkedHashMap<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("code", v.getCode() == null ? "" : v.getCode());
        m.put("name", v.getName() == null ? "" : v.getName());
        m.put("voucherType", v.getVoucherType() == null ? "" : v.getVoucherType());
        m.put("prefix", v.getPrefix() == null ? "" : v.getPrefix());
        m.put("suffix", v.getSuffix() == null ? "" : v.getSuffix());
        m.put("startNumber", v.getStartNumber() == null ? 1 : v.getStartNumber());
        m.put("currentNumber", v.getCurrentNumber() == null ? 1 : v.getCurrentNumber());
        m.put("description", v.getDescription() == null ? "" : v.getDescription());
        m.put("active", v.isActive());
        m.put("abbreviation", v.getAbbreviation() == null ? "" : v.getAbbreviation());
        m.put("period", v.getPeriod() == null ? "" : v.getPeriod());
        m.put("autoPosting", v.getAutoPosting() != null ? v.getAutoPosting() : true);
        m.put("autoPostToLedger", v.getAutoPostToLedger() != null ? v.getAutoPostToLedger() : false);
        m.put("groupCodes", v.getGroupCodes() == null ? "" : v.getGroupCodes());
        m.put("allowBackDateDays", v.getAllowBackDateDays() == null ? 0 : v.getAllowBackDateDays());
        m.put("voucherPost", v.getVoucherPost() != null ? v.getVoucherPost() : false);
        // The documents whose postings are numbered out of this book, so the screen shows the link.
        m.put("documentTypes", voucherBookService.documentTypesFor(v.getCode()));
        m.put("createdAt", v.getCreatedAt() == null ? "" : v.getCreatedAt().toString());
        return m;
    }
}
