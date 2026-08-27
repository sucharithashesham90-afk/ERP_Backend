package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.PeriodCloseDto;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.OpeningBalance;
import com.erp.platform.modules.accounting.entity.PeriodClose;
import com.erp.platform.modules.accounting.entity.PeriodClose.CloseStatus;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.OpeningBalanceRepository;
import com.erp.platform.modules.accounting.repository.PeriodCloseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PeriodCloseService {

    private final PeriodCloseRepository periodCloseRepository;
    private final AccountRepository accountRepository;
    private final OpeningBalanceRepository openingBalanceRepository;
    private final TenantContext tenantContext;

    public PageResponse<PeriodCloseDto> list(int year, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<PeriodClose> page = periodCloseRepository
                .findByTenantIdAndPeriodYearAndDeletedAtIsNull(tenantId, year, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public PeriodCloseDto getByPeriod(int year, int month) {
        UUID tenantId = tenantContext.current();
        PeriodClose pc = periodCloseRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .orElseThrow(() -> AppException.notFound(
                        "Period close not found for " + year + "/" + month));
        return toDto(pc);
    }

    @Transactional
    public PeriodCloseDto initiatePeriodClose(int year, int month, String closedBy) {
        UUID tenantId = tenantContext.current();

        periodCloseRepository.findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .ifPresent(pc -> {
                    if (pc.getStatus() == CloseStatus.CLOSED) {
                        throw AppException.businessRule(
                                "Period " + year + "/" + month + " is already closed");
                    }
                    if (pc.getStatus() == CloseStatus.CLOSING_IN_PROGRESS) {
                        throw AppException.businessRule(
                                "Period close already in progress for " + year + "/" + month);
                    }
                });

        PeriodClose pc = periodCloseRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .orElseGet(() -> {
                    PeriodClose newPc = new PeriodClose();
                    newPc.setTenantId(tenantId);
                    newPc.setPeriodYear(year);
                    newPc.setPeriodMonth(month);
                    newPc.setPeriodName(year + "-" + String.format("%02d", month));
                    return newPc;
                });

        pc.setStatus(CloseStatus.CLOSING_IN_PROGRESS);
        pc.setClosedBy(closedBy);
        PeriodClose saved = periodCloseRepository.save(pc);
        log.info("Period close initiated: {}/{} by {}", year, month, closedBy);
        return toDto(saved);
    }

    @Transactional
    public PeriodCloseDto completePeriodClose(int year, int month) {
        UUID tenantId = tenantContext.current();
        PeriodClose pc = periodCloseRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .orElseThrow(() -> AppException.notFound("Period close not found for " + year + "/" + month));

        if (pc.getStatus() != CloseStatus.CLOSING_IN_PROGRESS) {
            throw AppException.businessRule(
                    "Period close must be in CLOSING_IN_PROGRESS state to complete. Current: " + pc.getStatus());
        }

        pc.setStatus(CloseStatus.CLOSED);
        pc.setClosedAt(LocalDateTime.now());
        PeriodClose saved = periodCloseRepository.save(pc);
        log.info("Period close completed: {}/{}", year, month);
        return toDto(saved);
    }

    @Transactional
    public PeriodCloseDto reopenPeriod(int year, int month, String reason) {
        UUID tenantId = tenantContext.current();
        PeriodClose pc = periodCloseRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .orElseThrow(() -> AppException.notFound("Period close not found for " + year + "/" + month));

        if (pc.getStatus() != CloseStatus.CLOSED) {
            throw AppException.businessRule(
                    "Only CLOSED periods can be reopened. Current: " + pc.getStatus());
        }

        pc.setStatus(CloseStatus.REOPENED);
        pc.setOpenedAt(LocalDateTime.now());
        pc.setNotes(reason);
        PeriodClose saved = periodCloseRepository.save(pc);
        log.info("Period reopened: {}/{}, reason: {}", year, month, reason);
        return toDto(saved);
    }

    /**
     * Freeze a closed period: no user may post to a frozen period. Enabled only once CLOSED
     * (per the Closing of Accounts spec).
     */
    @Transactional
    public PeriodCloseDto freezePeriod(int year, int month, String frozenBy) {
        UUID tenantId = tenantContext.current();
        PeriodClose pc = periodCloseRepository
                .findByTenantIdAndPeriodYearAndPeriodMonthAndDeletedAtIsNull(tenantId, year, month)
                .orElseThrow(() -> AppException.notFound("Period close not found for " + year + "/" + month));

        if (pc.getStatus() != CloseStatus.CLOSED) {
            throw AppException.businessRule(
                    "Only CLOSED periods can be frozen. Current: " + pc.getStatus());
        }

        pc.setStatus(CloseStatus.FROZEN);
        pc.setFrozenAt(LocalDateTime.now());
        pc.setFrozenBy(frozenBy);
        PeriodClose saved = periodCloseRepository.save(pc);
        log.info("Period frozen: {}/{}", year, month);
        return toDto(saved);
    }

    /**
     * Post ledger balances: accumulate each ledger's balance and record it as the opening balance
     * for the next period, dated the upcoming 1st April.
     */
    @Transactional
    public Map<String, Object> postLedgerBalance() {
        UUID tenantId = tenantContext.current();

        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 4 ? today.getYear() + 1 : today.getYear();
        LocalDate nextAprilFirst = LocalDate.of(year, 4, 1);   // start of the next financial year

        List<Account> accounts = accountRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        int count = 0;
        for (Account a : accounts) {
            BigDecimal bal = a.getBalance() == null ? BigDecimal.ZERO : a.getBalance();
            if (bal.signum() == 0) continue;

            boolean debitNature = "ASSET".equalsIgnoreCase(a.getType()) || "EXPENSE".equalsIgnoreCase(a.getType());
            BigDecimal debit = BigDecimal.ZERO, credit = BigDecimal.ZERO;
            if (debitNature) {
                if (bal.signum() >= 0) debit = bal; else credit = bal.abs();
            } else {
                if (bal.signum() >= 0) credit = bal; else debit = bal.abs();
            }

            OpeningBalance ob = new OpeningBalance();
            ob.setTenantId(tenantId);
            ob.setBalanceType(OpeningBalance.BalanceType.GENERAL_LEDGER);
            ob.setAsOfDate(nextAprilFirst);
            ob.setAccountId(a.getId());
            ob.setAccountCode(a.getCode());
            ob.setAccountName(a.getName());
            ob.setDebitAmount(debit);
            ob.setCreditAmount(credit);
            ob.setStatus(OpeningBalance.MigrationStatus.POSTED);
            ob.setPostedAt(LocalDateTime.now());
            ob.setReference("CARRY_FORWARD");
            ob.setNotes("Opening balance carried forward for " + nextAprilFirst);
            openingBalanceRepository.save(ob);
            count++;
        }

        log.info("Posted ledger balances for {} accounts as of {}", count, nextAprilFirst);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("posted", count);
        result.put("asOfDate", nextAprilFirst.toString());
        return result;
    }

    private PeriodCloseDto toDto(PeriodClose pc) {
        PeriodCloseDto dto = new PeriodCloseDto();
        dto.setId(pc.getId());
        dto.setPeriodYear(pc.getPeriodYear());
        dto.setPeriodMonth(pc.getPeriodMonth());
        dto.setPeriodName(pc.getPeriodName());
        dto.setStatus(pc.getStatus());
        dto.setClosedAt(pc.getClosedAt());
        dto.setClosedBy(pc.getClosedBy());
        dto.setOpenedAt(pc.getOpenedAt());
        dto.setOpeningBalance(pc.getOpeningBalance());
        dto.setClosingBalance(pc.getClosingBalance());
        dto.setTotalDebits(pc.getTotalDebits());
        dto.setTotalCredits(pc.getTotalCredits());
        dto.setNotes(pc.getNotes());
        dto.setCreatedAt(pc.getCreatedAt());
        dto.setUpdatedAt(pc.getUpdatedAt());
        return dto;
    }
}
