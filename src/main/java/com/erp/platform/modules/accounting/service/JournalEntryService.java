package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.CreateJournalEntryRequest;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final VoucherBookService voucherBookService;
    private final TenantContext tenantContext;

    public PageResponse<JournalEntry> list(JEStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? journalEntryRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : journalEntryRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page);
    }

    public JournalEntry getById(UUID id) {
        return findOrThrow(id);
    }

    public boolean existsByReferenceIdOrNumber(UUID tenantId, UUID referenceId, String referenceNumber, java.util.Collection<String> types) {
        if (referenceId != null && journalEntryRepository.existsByTenantIdAndReferenceIdAndReferenceTypeInAndDeletedAtIsNull(tenantId, referenceId, types))
            return true;
        if (referenceNumber != null && !referenceNumber.isBlank() && journalEntryRepository.existsByTenantIdAndReferenceNumberAndReferenceTypeInAndDeletedAtIsNull(tenantId, referenceNumber, types))
            return true;
        return false;
    }

    @Transactional
    public JournalEntry create(JournalEntry entry) {
        UUID tenantId = tenantContext.current();
        entry.setTenantId(tenantId);
        // Numbers the voucher from the book its document type belongs to (Sales Invoices, Purchase
        // Invoices, …) rather than one generic series.
        voucherBookService.applyTo(entry);
        if (entry.getStatus() == null) entry.setStatus(JEStatus.DRAFT);
        if (entry.getEntryDate() == null) entry.setEntryDate(LocalDate.now());
        calculateTotals(entry);
        validateBalance(entry);
        if (entry.getLines() != null) entry.getLines().forEach(l -> l.setJournalEntry(entry));
        JournalEntry saved = journalEntryRepository.save(entry);
        log.info("JournalEntry created: id={}, number={}", saved.getId(), saved.getEntryNumber());
        return saved;
    }

    @Transactional
    public JournalEntry create(CreateJournalEntryRequest request) {
        UUID tenantId = tenantContext.current();

        JournalEntry entry = new JournalEntry();
        entry.setTenantId(tenantId);
        entry.setStatus(JEStatus.DRAFT);
        entry.setEntryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now());
        entry.setDescription(request.getDescription());
        entry.setReferenceType(request.getReferenceType());
        entry.setReferenceNumber(request.getReferenceNumber());
        entry.setPartyName(request.getPartyName());
        entry.setPartyAccountId(request.getPartyAccountId());
        entry.setTdsAmount(request.getTdsAmount());
        entry.setTdsRate(request.getTdsRate());
        entry.setPanNumber(request.getPanNumber());
        entry.setRemarks(request.getRemarks());
        entry.setVatAmount(request.getVatAmount());
        entry.setVatRate(request.getVatRate());
        voucherBookService.applyTo(entry);

        if (request.getLines() != null) {
            for (CreateJournalEntryRequest.LineRequest lr : request.getLines()) {
                JournalEntryLine line = new JournalEntryLine();
                line.setJournalEntry(entry);
                UUID resolvedId = lr.resolvedAccountId();
                String code = lr.resolvedAccountCode();
                String name = lr.resolvedAccountName();
                if (resolvedId == null && code != null && !code.isBlank()) {
                    resolvedId = accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code).map(Account::getId).orElse(null);
                }
                if (resolvedId == null && name != null && !name.isBlank()) {
                    resolvedId = accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name).map(Account::getId).orElse(null);
                }
                // Better to refuse the voucher than to book it against whichever ledger happens to
                // come back first — a line posted to the wrong account is far harder to find later
                // than a save that failed loudly.
                if (resolvedId == null) {
                    String shown = code != null && !code.isBlank() ? code
                                 : name != null && !name.isBlank() ? name : "(blank)";
                    throw AppException.badRequest(
                            "No ledger account matches '" + shown + "'. Pick an account from the chart of accounts.");
                }
                Account resolved = accountRepository.findById(resolvedId).orElse(null);
                line.setAccountId(resolvedId);
                line.setAccountCode(code != null && !code.isBlank() ? code : resolved != null ? resolved.getCode() : null);
                line.setAccountName(name != null && !name.isBlank() ? name : resolved != null ? resolved.getName() : null);
                line.setDebitAmount(lr.getDebitAmount() != null ? lr.getDebitAmount() : java.math.BigDecimal.ZERO);
                line.setCreditAmount(lr.getCreditAmount() != null ? lr.getCreditAmount() : java.math.BigDecimal.ZERO);
                line.setDescription(lr.getDescription());
                entry.getLines().add(line);
            }
        }

        calculateTotals(entry);
        validateBalance(entry);

        JournalEntry saved = journalEntryRepository.save(entry);
        log.info("JournalEntry created: id={}, number={}", saved.getId(), saved.getEntryNumber());
        return saved;
    }

    @Transactional
    public JournalEntry post(UUID id) {
        JournalEntry entry = findOrThrow(id);
        if (entry.getStatus() != JEStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT entries can be posted");
        }
        validateBalance(entry);
        if (entry.getLines() != null) {
            for (JournalEntryLine line : entry.getLines()) {
                Account account = accountService.getById(line.getAccountId());
                BigDecimal netEffect = computeNetEffect(account.getType(), line.getDebitAmount(), line.getCreditAmount());
                accountService.adjustBalance(account.getId(), netEffect);
            }
        }
        entry.setStatus(JEStatus.POSTED);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry cancel(UUID id) {
        JournalEntry entry = findOrThrow(id);
        if (entry.getStatus() == JEStatus.CANCELLED) {
            throw AppException.badRequest("Entry is already cancelled");
        }
        if (entry.getStatus() == JEStatus.POSTED) {
            if (entry.getLines() != null) {
                for (JournalEntryLine line : entry.getLines()) {
                    Account account = accountService.getById(line.getAccountId());
                    BigDecimal reversal = computeNetEffect(account.getType(), line.getDebitAmount(), line.getCreditAmount()).negate();
                    accountService.adjustBalance(account.getId(), reversal);
                }
            }
        }
        entry.setStatus(JEStatus.CANCELLED);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public void delete(UUID id) {
        JournalEntry entry = findOrThrow(id);
        if (entry.getStatus() == JEStatus.POSTED) {
            throw AppException.badRequest("Cannot delete a POSTED journal entry");
        }
        entry.setDeletedAt(LocalDateTime.now());
        journalEntryRepository.save(entry);
        log.info("JournalEntry soft-deleted: id={}", id);
    }

    private void calculateTotals(JournalEntry entry) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        if (entry.getLines() != null) {
            for (JournalEntryLine line : entry.getLines()) {
                totalDebit = totalDebit.add(line.getDebitAmount() != null ? line.getDebitAmount() : BigDecimal.ZERO);
                totalCredit = totalCredit.add(line.getCreditAmount() != null ? line.getCreditAmount() : BigDecimal.ZERO);
            }
        }
        entry.setTotalDebit(totalDebit);
        entry.setTotalCredit(totalCredit);
    }

    private void validateBalance(JournalEntry entry) {
        if (entry.getTotalDebit().compareTo(entry.getTotalCredit()) != 0) {
            throw AppException.badRequest("Journal entry is not balanced: debit=" + entry.getTotalDebit()
                    + ", credit=" + entry.getTotalCredit());
        }
    }

    private BigDecimal computeNetEffect(String accountType, BigDecimal debit, BigDecimal credit) {
        debit = debit != null ? debit : BigDecimal.ZERO;
        credit = credit != null ? credit : BigDecimal.ZERO;
        boolean normalDebit = "ASSET".equals(accountType) || "EXPENSE".equals(accountType);
        return normalDebit ? debit.subtract(credit) : credit.subtract(debit);
    }

    private JournalEntry findOrThrow(UUID id) {
        return journalEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Journal entry not found: " + id));
    }
}
