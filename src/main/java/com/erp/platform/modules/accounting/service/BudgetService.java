package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.BudgetEntryDto;
import com.erp.platform.modules.accounting.dto.CreateBudgetEntryRequest;
import com.erp.platform.modules.accounting.entity.BudgetEntry;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.BudgetEntryRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetEntryRepository budgetEntryRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final TenantContext tenantContext;

    public PageResponse<BudgetEntryDto> list(int year, int month, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<BudgetEntry> page = budgetEntryRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    @Transactional
    public BudgetEntryDto create(CreateBudgetEntryRequest request) {
        UUID tenantId = tenantContext.current();

        BudgetEntry entry = new BudgetEntry();
        entry.setTenantId(tenantId);
        entry.setPeriodYear(request.getPeriodYear());
        entry.setPeriodMonth(request.getPeriodMonth());
        entry.setAccountId(resolveAccountId(request.getAccountId(), request.getAccountCode(), tenantId));
        entry.setAccountCode(request.getAccountCode());
        entry.setAccountName(request.getAccountName());
        entry.setCostCenterId(request.getCostCenterId());
        entry.setDimensionValueId(request.getDimensionValueId());
        entry.setBudgetAmount(request.getBudgetAmount());
        entry.setActualAmount(request.getActualAmount() != null ? request.getActualAmount() : BigDecimal.ZERO);
        entry.setNotes(request.getNotes());

        BudgetEntry saved = budgetEntryRepository.save(entry);
        log.info("BudgetEntry created: id={}, period={}/{}", saved.getId(), saved.getPeriodYear(), saved.getPeriodMonth());
        return toDto(saved);
    }

    @Transactional
    public BudgetEntryDto update(UUID id, CreateBudgetEntryRequest request) {
        UUID tenantId = tenantContext.current();
        BudgetEntry entry = budgetEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Budget entry not found: " + id));

        entry.setPeriodYear(request.getPeriodYear());
        entry.setPeriodMonth(request.getPeriodMonth());
        entry.setAccountId(resolveAccountId(request.getAccountId(), request.getAccountCode(), tenantId));
        entry.setAccountCode(request.getAccountCode());
        entry.setAccountName(request.getAccountName());
        entry.setCostCenterId(request.getCostCenterId());
        entry.setDimensionValueId(request.getDimensionValueId());
        entry.setBudgetAmount(request.getBudgetAmount());
        if (request.getActualAmount() != null) {
            entry.setActualAmount(request.getActualAmount());
        }
        entry.setNotes(request.getNotes());

        BudgetEntry saved = budgetEntryRepository.save(entry);
        log.info("BudgetEntry updated: id={}", id);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        BudgetEntry entry = budgetEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Budget entry not found: " + id));
        entry.setDeletedAt(java.time.LocalDateTime.now());
        budgetEntryRepository.save(entry);
        log.info("BudgetEntry deleted: id={}", id);
    }

    public List<BudgetEntryDto> getBudgetVsActual(int year, int month) {
        UUID tenantId = tenantContext.current();
        List<BudgetEntry> entries = budgetEntryRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month);
        return entries.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> syncActuals(int year, int month) {
        UUID tenantId = tenantContext.current();
        List<BudgetEntry> entries = budgetEntryRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month);

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        int updated = 0;
        for (BudgetEntry entry : entries) {
            if (entry.getAccountCode() == null || entry.getAccountCode().isBlank()) continue;
            BigDecimal debits  = journalEntryRepository.sumDebitsByAccountAndPeriod(tenantId, entry.getAccountCode(), from, to);
            BigDecimal credits = journalEntryRepository.sumCreditsByAccountAndPeriod(tenantId, entry.getAccountCode(), from, to);
            // For expense/asset accounts actual = net debits; for income/liability accounts actual = net credits
            BigDecimal actual = (debits != null ? debits : BigDecimal.ZERO)
                    .subtract(credits != null ? credits : BigDecimal.ZERO).abs();
            entry.setActualAmount(actual);
            budgetEntryRepository.save(entry);
            updated++;
        }
        log.info("Budget actuals synced: year={}, month={}, entries={}", year, month, updated);
        return Map.of("year", year, "month", month, "entriesUpdated", updated);
    }

    private UUID resolveAccountId(UUID provided, String accountCode, UUID tenantId) {
        if (provided != null) return provided;
        if (accountCode != null && !accountCode.isBlank()) {
            return accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, accountCode)
                    .map(a -> a.getId())
                    .orElse(null);
        }
        return null;
    }

    private BudgetEntryDto toDto(BudgetEntry e) {
        BudgetEntryDto dto = new BudgetEntryDto();
        dto.setId(e.getId());
        dto.setPeriodYear(e.getPeriodYear());
        dto.setPeriodMonth(e.getPeriodMonth());
        dto.setAccountId(e.getAccountId());
        dto.setAccountCode(e.getAccountCode());
        dto.setAccountName(e.getAccountName());
        dto.setCostCenterId(e.getCostCenterId());
        dto.setDimensionValueId(e.getDimensionValueId());
        dto.setBudgetAmount(e.getBudgetAmount());
        dto.setActualAmount(e.getActualAmount());
        dto.setVariance(e.getVariance());
        dto.setNotes(e.getNotes());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }
}
