package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.PeriodDefinition;
import com.erp.platform.modules.accounting.entity.VoucherBook;
import com.erp.platform.modules.accounting.repository.PeriodDefinitionRepository;
import com.erp.platform.modules.accounting.repository.VoucherBookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/accounting/period-definitions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Period Definition", description = "Financial Year Period Definition management")
public class PeriodDefinitionController {

    private final PeriodDefinitionRepository repo;
    private final VoucherBookRepository voucherBookRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List period definitions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        UUID tenantId = tenantContext.current();
        List<PeriodDefinition> list = repo.findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(tenantId);
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (PeriodDefinition pd : list) {
            result.add(toMap(pd, fmt));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate next period definition")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateNext() {
        UUID tenantId = tenantContext.current();
        Optional<PeriodDefinition> latestOpt = repo.findFirstByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(tenantId);

        int startYear;
        if (latestOpt.isPresent()) {
            startYear = latestOpt.get().getStartDate().getYear() + 1;
        } else {
            LocalDate now = LocalDate.now();
            startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        }

        int endYear = startYear + 1;
        LocalDate startDate = LocalDate.of(startYear, 4, 1);
        LocalDate endDate = LocalDate.of(endYear, 3, 31);

        String startYearShort = String.format("%02d", startYear % 100);
        String endYearShort = String.format("%02d", endYear % 100);
        String periodCode = startYearShort + "-" + endYearShort;

        PeriodDefinition pd = new PeriodDefinition();
        pd.setTenantId(tenantId);
        pd.setStartDate(startDate);
        pd.setEndDate(endDate);
        pd.setPeriodType("Yearly");
        pd.setPeriodCode(periodCode);
        pd.setPeriodStatus("Initialized");

        PeriodDefinition saved = repo.save(pd);
        autoCreateVoucherBooks(tenantId, saved);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toMap(saved, fmt), "Period generated and voucher books auto-created"));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Toggle period status between Initialized and Freezed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleStatus(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        PeriodDefinition pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Period definition not found: " + id));

        if ("Freezed".equalsIgnoreCase(pd.getPeriodStatus())) {
            pd.setPeriodStatus("Initialized");
        } else {
            pd.setPeriodStatus("Freezed");
        }

        PeriodDefinition saved = repo.save(pd);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return ResponseEntity.ok(ApiResponse.success(toMap(saved, fmt), "Period status updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete period definition")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID tenantId = tenantContext.current();
        PeriodDefinition pd = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Period definition not found: " + id));
        pd.setDeletedAt(LocalDateTime.now());
        repo.save(pd);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void autoCreateVoucherBooks(UUID tenantId, PeriodDefinition pd) {
        if (pd == null || voucherBookRepository == null) return;
        structBook(tenantId, "JE", "Journal Entries", "JE", "JOURNAL", "");
        structBook(tenantId, "BP", "Bank Payments", "BP", "PAYMENT", "");
        structBook(tenantId, "BR", "Bank Receipts", "BR", "RECEIPT", "");
        structBook(tenantId, "PI", "Purchase Invoices", "PI", "PURCHASE", "");
        structBook(tenantId, "SI", "Sales Invoices", "SI", "SALES", "");
        structBook(tenantId, "CP", "Cash Payments", "CP", "PAYMENT", "");
        structBook(tenantId, "CR", "Cash Receipts", "CR", "RECEIPT", "");
    }

    private void structBook(UUID tenantId, String code, String name, String abbr, String type, String period) {
        // existsBy (not findBy) — a findBy returning Optional blows up with an
        // IncorrectResultSizeDataAccessException when legacy duplicate books are still present,
        // which would fail the whole period generation.
        if (voucherBookRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)) return;
        VoucherBook vb = new VoucherBook();
        vb.setTenantId(tenantId);
        vb.setCode(code);
        vb.setName(name);
        vb.setAbbreviation(abbr);
        vb.setVoucherType(type);
        vb.setPeriod(period);
        vb.setStartNumber(1);
        vb.setCurrentNumber(1);
        vb.setActive(true);
        vb.setAutoPosting(true);
        vb.setAutoPostToLedger(true);
        voucherBookRepository.save(vb);
    }

    private Map<String, Object> toMap(PeriodDefinition pd, DateTimeFormatter fmt) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pd.getId());
        m.put("startDate", pd.getStartDate() != null ? pd.getStartDate().format(fmt) : "");
        m.put("endDate", pd.getEndDate() != null ? pd.getEndDate().format(fmt) : "");
        m.put("periodType", pd.getPeriodType());
        m.put("periodCode", pd.getPeriodCode());
        m.put("periodStatus", pd.getPeriodStatus());
        m.put("rawStartDate", pd.getStartDate() != null ? pd.getStartDate().toString() : "");
        return m;
    }
}
