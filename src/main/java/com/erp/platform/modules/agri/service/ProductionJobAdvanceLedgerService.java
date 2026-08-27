package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.agri.entity.ProductionJobAdvance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Posts producer advances into the accounting ledgers: Dr "Advance to Producer" (asset),
 * Cr Cash / Bank. Best-effort — a missing account is logged and skipped rather than failing
 * the advance, so the operational record is never blocked by chart-of-accounts gaps.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionJobAdvanceLedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    @Transactional
    public void postAdvance(ProductionJobAdvance adv) {
        if (adv.getAmount() == null || adv.getAmount().compareTo(BigDecimal.ZERO) <= 0) return;
        UUID tenantId = tenantContext.current();

        Account advanceAcct = bySubType(tenantId, "ADVANCE_TO_VENDOR");
        boolean cash = adv.getPaymentMethod() == null || adv.getPaymentMethod().equalsIgnoreCase("CASH");
        Account payAcct = cash ? bySubType(tenantId, "CASH") : bySubType(tenantId, "BANK", "BANK_ACCOUNT");
        if (advanceAcct == null || payAcct == null) {
            log.warn("Skipping ledger post for advance {} — advance/cash/bank account not found in chart of accounts",
                    adv.getAdvanceNumber());
            return;
        }

        try {
            JournalEntry je = new JournalEntry();
            je.setReferenceType("PRODUCTION_JOB_ADVANCE");
            je.setReferenceId(adv.getId());
            je.setReferenceNumber(adv.getAdvanceNumber());
            je.setEntryDate(adv.getAdvanceDate());
            je.setDescription("Advance to " + adv.getAllocateeType() + " " + adv.getAllocateeName()
                    + (adv.getJobNumber() != null ? " (job " + adv.getJobNumber() + ")" : ""));

            je.getLines().add(line(advanceAcct, adv.getAmount(), BigDecimal.ZERO, "Advance to producer"));
            je.getLines().add(line(payAcct, BigDecimal.ZERO, adv.getAmount(), cash ? "Cash paid" : "Bank paid"));

            JournalEntry saved = journalEntryService.create(je);
            journalEntryService.post(saved.getId());
            log.info("Posted advance {} to ledger as journal {}", adv.getAdvanceNumber(), saved.getEntryNumber());
        } catch (Exception e) {
            log.warn("Ledger post failed for advance {}: {}", adv.getAdvanceNumber(), e.getMessage());
        }
    }

    private JournalEntryLine line(Account acct, BigDecimal debit, BigDecimal credit, String desc) {
        JournalEntryLine l = new JournalEntryLine();
        l.setAccountId(acct.getId());
        l.setAccountCode(acct.getCode());
        l.setAccountName(acct.getName());
        l.setDebitAmount(debit);
        l.setCreditAmount(credit);
        l.setDescription(desc);
        return l;
    }

    private Account bySubType(UUID tenantId, String... subTypes) {
        for (String st : subTypes) {
            List<Account> found = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, st);
            if (found != null && !found.isEmpty()) return found.get(0);
        }
        return null;
    }
}
