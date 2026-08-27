package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import com.erp.platform.modules.agri.repository.FarmerRepository;
import com.erp.platform.modules.agri.repository.OrganizerRepository;
import com.erp.platform.modules.purchase.entity.PaymentLiability;
import com.erp.platform.modules.purchase.repository.PaymentLiabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pays a grower or organizer liability and posts the voucher for it.
 *
 * <p>Recording the intake creates the liability - what is owed for what was delivered. Until it is
 * paid nothing reaches the books, so the grower's ledger showed the amount owing and never showed it
 * settled. This posts the payment: the party's ledger is debited, clearing what is owed, and the
 * bank or cash account credited for what left. The entry carries the liability number as its
 * reference, so it is findable from Voucher Search and shows on Ledger Search under that grower.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiabilityPaymentService {

    private final PaymentLiabilityRepository liabilityRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final JournalEntryRepository journalEntryRepository;
    private final PartyLedgerService partyLedgerService;
    private final FarmerRepository farmerRepository;
    private final OrganizerRepository organizerRepository;
    private final TenantContext tenantContext;

    private static final String REF_TYPE = "LIABILITY_PAYMENT";

    @Transactional
    public Map<String, Object> pay(UUID liabilityId, BigDecimal amount, String paymentMode,
                                   String reference, LocalDate paymentDate) {

        UUID tenantId = tenantContext.current();
        PaymentLiability liab = liabilityRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, liabilityId)
                .orElseThrow(() -> AppException.notFound("Payment liability not found: " + liabilityId));

        BigDecimal outstanding = nz(liab.getBalance()).signum() > 0
                ? nz(liab.getBalance())
                : nz(liab.getTotalLiability()).subtract(nz(liab.getPaidAmount()));

        BigDecimal pay = amount != null && amount.signum() > 0 ? amount : outstanding;
        if (pay.signum() <= 0) throw AppException.badRequest("Nothing outstanding on this liability");
        if (pay.compareTo(outstanding) > 0) {
            throw AppException.badRequest("Payment of " + pay.toPlainString()
                    + " is more than the " + outstanding.toPlainString() + " outstanding");
        }

        Account partyLedger = resolvePartyLedger(tenantId, liab);
        Account fundsAccount = resolveFundsAccount(tenantId, paymentMode);

        // Paying reduces what is owed: the liability ledger is debited, the money account credited.
        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setEntryDate(paymentDate != null ? paymentDate : LocalDate.now());
        je.setReferenceType(REF_TYPE);
        je.setReferenceId(liab.getId());
        je.setReferenceNumber(liab.getLiabilityNumber());
        je.setDescription("Liability payment: " + liab.getLiabilityNumber()
                + " — " + blankTo(liab.getPartyName(), "Party")
                + (liab.getLotNumber() != null && !liab.getLotNumber().isBlank() ? " (lot " + liab.getLotNumber() + ")" : "")
                + (reference != null && !reference.isBlank() ? " ref " + reference : ""));
        je.getLines().add(line(partyLedger, pay, BigDecimal.ZERO, "Liability settled"));
        je.getLines().add(line(fundsAccount, BigDecimal.ZERO, pay, "Paid via " + blankTo(paymentMode, "BANK")));

        JournalEntry saved = journalEntryService.create(je);
        journalEntryService.post(saved.getId());

        BigDecimal paidNow = nz(liab.getPaidAmount()).add(pay);
        liab.setPaidAmount(paidNow);
        liab.setBalance(nz(liab.getTotalLiability()).subtract(paidNow));
        liab.setStatus(nz(liab.getBalance()).signum() <= 0 ? "PAID" : "PARTIALLY_PAID");
        liabilityRepository.save(liab);

        log.info("Liability {} paid {} to {}; voucher {}",
                liab.getLiabilityNumber(), pay, liab.getPartyName(), saved.getEntryNumber());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("liabilityId", liab.getId());
        out.put("liabilityNumber", liab.getLiabilityNumber());
        out.put("partyName", liab.getPartyName());
        out.put("amountPaid", pay);
        out.put("paidAmount", liab.getPaidAmount());
        out.put("balance", liab.getBalance());
        out.put("status", liab.getStatus());
        out.put("voucherId", saved.getId());
        out.put("voucherNumber", saved.getEntryNumber());
        out.put("partyLedger", partyLedger.getName());
        out.put("fundsAccount", fundsAccount.getName());
        return out;
    }

    /** Has this liability already been paid into the books? Guards a double post on a retry. */
    public boolean alreadyPosted(UUID tenantId, UUID liabilityId) {
        return journalEntryRepository.existsByTenantIdAndReferenceIdAndReferenceTypeInAndDeletedAtIsNull(
                tenantId, liabilityId, java.util.List.of(REF_TYPE));
    }

    /**
     * The grower's or organizer's own ledger.
     *
     * <p>Resolved by the deterministic per-party code where the party can be identified, so payments
     * land on the same ledger the intake created rather than on a second one spelled slightly
     * differently. Falls back to the name only when the party cannot be matched.
     */
    private Account resolvePartyLedger(UUID tenantId, PaymentLiability liab) {
        String type = liab.getPartyType() == null ? "" : liab.getPartyType().toUpperCase();
        String name = blankTo(liab.getPartyName(), null);
        if (name == null) throw AppException.badRequest("This liability has no party to pay");

        UUID partyId;
        final PartyLedgerService.PartyType party;
        if (type.contains("ORGANIZER")) {
            party = PartyLedgerService.PartyType.ORGANIZER;
            partyId = organizerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000))
                    .getContent().stream()
                    .filter(o -> name.equalsIgnoreCase(o.getName()))
                    .map(o -> o.getId()).findFirst().orElse(null);
        } else {
            party = PartyLedgerService.PartyType.GROWER;
            partyId = farmerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 5000))
                    .getContent().stream()
                    .filter(f -> name.equalsIgnoreCase(f.getName()))
                    .map(f -> f.getId()).findFirst().orElse(null);
        }

        if (partyId != null) {
            partyLedgerService.ensureLedger(party, name, partyId, liab.getVendorCode());
            String code = partyLedgerService.ledgerCode(party, partyId);
            var byCode = accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code);
            if (byCode.isPresent()) return byCode.get();
        }

        return accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name)
                .orElseGet(() -> createAccount(tenantId, name, "LIABILITY",
                        party == PartyLedgerService.PartyType.ORGANIZER ? "ORGANIZERS" : "GROWERS",
                        uniqueCode(tenantId, "PARTY")));
    }

    /** Where the money went out of - cash for a cash payment, the bank account otherwise. */
    private Account resolveFundsAccount(UUID tenantId, String paymentMode) {
        boolean cash = paymentMode != null && paymentMode.toUpperCase().contains("CASH");
        String code = cash ? "CASH" : "BANK";
        String name = cash ? "Cash" : "Bank";
        return accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .or(() -> accountRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name))
                .orElseGet(() -> createAccount(tenantId, name, "ASSET", cash ? "CASH" : "BANK", code));
    }

    private Account createAccount(UUID tenantId, String name, String type, String subType, String code) {
        Account a = new Account();
        a.setTenantId(tenantId);
        a.setName(name);
        a.setType(type);
        a.setSubType(subType);
        a.setCode(code);
        a.setActive(true);
        a.setBalance(BigDecimal.ZERO);
        return accountRepository.save(a);
    }

    private String uniqueCode(UUID tenantId, String prefix) {
        long base = System.currentTimeMillis() % 100000;
        for (int i = 0; i < 1000; i++) {
            String code = prefix + "-" + (base + i);
            if (accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code).isEmpty()) return code;
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static JournalEntryLine line(Account a, BigDecimal debit, BigDecimal credit, String note) {
        JournalEntryLine l = new JournalEntryLine();
        l.setAccountId(a.getId());
        l.setAccountCode(a.getCode());
        l.setAccountName(a.getName());
        l.setDebitAmount(debit);
        l.setCreditAmount(credit);
        l.setDescription(note + " - " + a.getName());
        return l;
    }

    private static BigDecimal nz(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    private static String blankTo(String v, String fallback) {
        return v != null && !v.isBlank() ? v : fallback;
    }
}
