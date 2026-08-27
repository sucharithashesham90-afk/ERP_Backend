package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.BankAccount;
import com.erp.platform.modules.accounting.entity.BankReconciliation;
import com.erp.platform.modules.accounting.entity.BankReconciliation.ReconciliationStatus;
import com.erp.platform.modules.accounting.entity.BankStatement;
import com.erp.platform.modules.accounting.repository.BankAccountRepository;
import com.erp.platform.modules.accounting.repository.BankReconciliationRepository;
import com.erp.platform.modules.accounting.repository.BankStatementRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BankReconciliationService {

    private final BankAccountRepository bankAccountRepo;
    private final BankReconciliationRepository reconciliationRepo;
    private final BankStatementRepository statementRepo;
    private final JournalEntryRepository journalEntryRepo;
    private final TenantContext tenantContext;

    public PageResponse<BankAccount> listAccounts(Pageable pageable) {
        return PageResponse.of(bankAccountRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    @Transactional
    public BankAccount createAccount(BankAccount req) {
        req.setTenantId(tenantContext.current());
        return bankAccountRepo.save(req);
    }

    @Transactional
    public BankAccount updateAccount(UUID id, BankAccount req) {
        BankAccount e = bankAccountRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Bank account not found: " + id));
        e.setAccountName(req.getAccountName());
        e.setAccountNumber(req.getAccountNumber());
        e.setBankName(req.getBankName());
        e.setIfscCode(req.getIfscCode());
        e.setBranch(req.getBranch());
        e.setOpeningBalance(req.getOpeningBalance());
        e.setCurrentBalance(req.getCurrentBalance());
        e.setCurrency(req.getCurrency());
        e.setActive(req.isActive());
        return bankAccountRepo.save(e);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        BankAccount e = bankAccountRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Bank account not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        bankAccountRepo.save(e);
    }

    public PageResponse<BankReconciliation> listReconciliations(UUID accountId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (accountId != null) {
            return PageResponse.of(reconciliationRepo.findByTenantIdAndBankAccountIdAndDeletedAtIsNull(tenantId, accountId, pageable));
        }
        return PageResponse.of(reconciliationRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public BankReconciliation createReconciliation(BankReconciliation req) {
        req.setTenantId(tenantContext.current());
        req.setStatus(ReconciliationStatus.DRAFT);
        return reconciliationRepo.save(req);
    }

    public List<BankStatement> getEntries(UUID reconciliationId) {
        findReconciliationOrThrow(reconciliationId);
        return statementRepo.findByReconciliationIdOrderByTransactionDate(reconciliationId);
    }

    @Transactional
    public BankStatement addStatementEntry(UUID reconciliationId, BankStatement entry) {
        BankReconciliation reconciliation = findReconciliationOrThrow(reconciliationId);
        entry.setReconciliation(reconciliation);
        entry.setTenantId(tenantContext.current());
        entry.setMatched(false);
        if (reconciliation.getStatus() == ReconciliationStatus.DRAFT) {
            reconciliation.setStatus(ReconciliationStatus.IN_PROGRESS);
            reconciliationRepo.save(reconciliation);
        }
        return statementRepo.save(entry);
    }

    @Transactional
    public BankStatement matchEntry(UUID reconciliationId, UUID statementId, UUID voucherId) {
        findReconciliationOrThrow(reconciliationId);
        BankStatement entry = statementRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), statementId)
                .orElseThrow(() -> AppException.notFound("Statement entry not found: " + statementId));
        entry.setMatched(true);
        entry.setMatchedVoucherId(voucherId);
        return statementRepo.save(entry);
    }

    @Transactional
    public BankStatement unmatchEntry(UUID reconciliationId, UUID statementId) {
        findReconciliationOrThrow(reconciliationId);
        BankStatement entry = statementRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), statementId)
                .orElseThrow(() -> AppException.notFound("Statement entry not found: " + statementId));
        entry.setMatched(false);
        entry.setMatchedVoucherId(null);
        return statementRepo.save(entry);
    }

    @Transactional
    public Map<String, Object> autoMatch(UUID reconciliationId) {
        UUID tenantId = tenantContext.current();
        BankReconciliation recon = findReconciliationOrThrow(reconciliationId);
        BankAccount account = recon.getBankAccount();

        // Build GL transaction index: net amount → list of JE IDs for the period
        List<Object[]> glLines = journalEntryRepo.findLinesByAccountAndPeriod(
                tenantId, account.getAccountNumber(),
                recon.getStatementDate().withDayOfMonth(1),
                recon.getStatementDate());

        // Map: net-amount → JE UUID (only keep entries with unique amounts)
        Map<BigDecimal, UUID> uniqueAmounts = new HashMap<>();
        Map<BigDecimal, Integer> amountCount = new HashMap<>();
        for (Object[] row : glLines) {
            BigDecimal dr = row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO;
            BigDecimal cr = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            UUID jeId = (UUID) row[3];
            BigDecimal net = dr.subtract(cr).abs();
            amountCount.merge(net, 1, Integer::sum);
            uniqueAmounts.put(net, jeId);
        }

        List<BankStatement> unmatched = statementRepo.findByReconciliationIdOrderByTransactionDate(reconciliationId)
                .stream().filter(e -> !e.isMatched()).toList();

        int matched = 0;
        for (BankStatement entry : unmatched) {
            BigDecimal entryAmount = entry.getCreditAmount().subtract(entry.getDebitAmount()).abs();
            // Only auto-match if exactly one GL line has this amount
            if (amountCount.getOrDefault(entryAmount, 0) == 1) {
                UUID jeId = uniqueAmounts.get(entryAmount);
                entry.setMatched(true);
                entry.setMatchedVoucherId(jeId);
                statementRepo.save(entry);
                matched++;
            }
        }

        log.info("Auto-match for reconciliation {}: matched {}/{} entries", reconciliationId, matched, unmatched.size());
        return Map.of("matched", matched, "unmatched", unmatched.size() - matched, "total", unmatched.size());
    }

    @Transactional
    public BankReconciliation closeReconciliation(UUID reconciliationId) {
        BankReconciliation recon = findReconciliationOrThrow(reconciliationId);
        UUID tenantId = tenantContext.current();
        BankAccount account = recon.getBankAccount();

        // Compute system balance from posted GL entries for the bank account
        BigDecimal glDebits = journalEntryRepo.sumDebitsByAccountAndPeriod(
                tenantId, account.getAccountNumber(),
                recon.getStatementDate().withDayOfMonth(1),
                recon.getStatementDate());
        BigDecimal glCredits = journalEntryRepo.sumCreditsByAccountAndPeriod(
                tenantId, account.getAccountNumber(),
                recon.getStatementDate().withDayOfMonth(1),
                recon.getStatementDate());
        BigDecimal systemBalance = account.getOpeningBalance()
                .add(glDebits != null ? glDebits : BigDecimal.ZERO)
                .subtract(glCredits != null ? glCredits : BigDecimal.ZERO);

        recon.setSystemBalance(systemBalance);
        recon.setDifference(recon.getStatementBalance().subtract(systemBalance));
        recon.setStatus(ReconciliationStatus.RECONCILED);
        return reconciliationRepo.save(recon);
    }

    private BankReconciliation findReconciliationOrThrow(UUID id) {
        return reconciliationRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Reconciliation not found: " + id));
    }
}
