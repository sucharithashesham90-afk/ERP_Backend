package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.FiscalYearDto;
import com.erp.platform.modules.accounting.entity.FiscalYear;
import com.erp.platform.modules.accounting.repository.FiscalYearRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FiscalYearService {

    private final FiscalYearRepository fiscalYearRepository;
    private final com.erp.platform.modules.accounting.repository.VoucherBookRepository voucherBookRepository;
    private final TenantContext tenantContext;

    public PageResponse<FiscalYearDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(fiscalYearRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public List<FiscalYearDto> listAll() {
        UUID tenantId = tenantContext.current();
        return fiscalYearRepository.findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(tenantId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public FiscalYearDto create(Map<String, Object> request) {
        UUID tenantId = tenantContext.current();
        String periodCode = (String) request.get("periodCode");
        if (periodCode == null || periodCode.isBlank()) throw AppException.badRequest("Period code is required");
        if (fiscalYearRepository.existsByTenantIdAndPeriodCodeAndDeletedAtIsNull(tenantId, periodCode)) {
            throw AppException.badRequest("Period code already exists: " + periodCode);
        }

        FiscalYear fy = new FiscalYear();
        fy.setTenantId(tenantId);
        fy.setPeriodCode(periodCode);
        fy.setStartDate(LocalDate.parse((String) request.get("startDate")));
        fy.setEndDate(LocalDate.parse((String) request.get("endDate")));
        fy.setPeriodType(request.getOrDefault("periodType", "YEARLY").toString());
        fy.setDescription((String) request.get("description"));
        fy.setStatus("INITIALIZED");

        fy = fiscalYearRepository.save(fy);
        autoCreateVoucherBooks(tenantId, fy);
        log.info("Fiscal year created: {} ({})", fy.getPeriodCode(), fy.getId());
        return toDto(fy);
    }

    /**
     * Generate the next yearly period (Apr 1 – Mar 31) from the latest record. Period type is always
     * YEARLY and the code is "startYear-endYY" (e.g. 2026-27).
     */
    @Transactional
    public FiscalYearDto generateNext() {
        UUID tenantId = tenantContext.current();
        List<FiscalYear> existing = fiscalYearRepository.findByTenantIdAndDeletedAtIsNullOrderByStartDateDesc(tenantId);

        LocalDate start;
        if (existing.isEmpty()) {
            LocalDate today = LocalDate.now();
            int startYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
            start = LocalDate.of(startYear, 4, 1);
        } else {
            LocalDate lastEnd = existing.get(0).getEndDate();
            start = lastEnd != null ? lastEnd.plusDays(1)
                    : LocalDate.of(LocalDate.now().getYear(), 4, 1);
        }
        LocalDate end = start.plusYears(1).minusDays(1);   // Mar 31 of the following year
        String code = start.getYear() + "-" + String.format("%02d", end.getYear() % 100);
        if (fiscalYearRepository.existsByTenantIdAndPeriodCodeAndDeletedAtIsNull(tenantId, code)) {
            throw AppException.badRequest("Period already exists: " + code);
        }

        FiscalYear fy = new FiscalYear();
        fy.setTenantId(tenantId);
        fy.setPeriodCode(code);
        fy.setStartDate(start);
        fy.setEndDate(end);
        fy.setPeriodType("YEARLY");
        fy.setStatus("INITIALIZED");
        fy.setDescription("Financial Year " + code);
        fy = fiscalYearRepository.save(fy);
        autoCreateVoucherBooks(tenantId, fy);
        log.info("Generated next fiscal year: {} ({} to {})", code, start, end);
        return toDto(fy);
    }

    private void autoCreateVoucherBooks(UUID tenantId, FiscalYear fy) {
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
        com.erp.platform.modules.accounting.entity.VoucherBook vb = new com.erp.platform.modules.accounting.entity.VoucherBook();
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

    @Transactional
    public FiscalYearDto close(UUID id) {
        UUID tenantId = tenantContext.current();
        FiscalYear fy = fiscalYearRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Fiscal year not found: " + id));
        if ("CLOSED".equals(fy.getStatus())) throw AppException.badRequest("Fiscal year is already closed");
        fy.setStatus("CLOSED");
        fy = fiscalYearRepository.save(fy);
        log.info("Fiscal year closed: {}", fy.getPeriodCode());
        return toDto(fy);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        FiscalYear fy = fiscalYearRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Fiscal year not found: " + id));
        if ("CLOSED".equals(fy.getStatus())) throw AppException.badRequest("Cannot delete a closed fiscal year");
        fy.setDeletedAt(LocalDateTime.now());
        fiscalYearRepository.save(fy);
    }

    private FiscalYearDto toDto(FiscalYear fy) {
        FiscalYearDto dto = new FiscalYearDto();
        dto.setId(fy.getId());
        dto.setTenantId(fy.getTenantId());
        dto.setPeriodCode(fy.getPeriodCode());
        dto.setStartDate(fy.getStartDate());
        dto.setEndDate(fy.getEndDate());
        dto.setPeriodType(fy.getPeriodType());
        dto.setStatus(fy.getStatus());
        dto.setDescription(fy.getDescription());
        dto.setCreatedAt(fy.getCreatedAt());
        return dto;
    }
}
