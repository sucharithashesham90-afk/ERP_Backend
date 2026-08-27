package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.AccountGroup;
import com.erp.platform.modules.accounting.repository.AccountGroupRepository;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Auto-maintains a per-party ledger (account) so every grower, organizer, customer and employee
 * has a matching ledger carrying the party's name. Idempotent: creates the ledger on first call and
 * keeps its name in sync on later calls, keyed by a stable per-party ledger code (derived from the
 * party's id, so renames follow the party). Best-effort — never fails the party save.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartyLedgerService {

    private final AccountRepository accountRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final TenantContext tenantContext;

    /** Party kind → ledger code prefix, account group (name + code), account type and sub-type. */
    public enum PartyType {
        CUSTOMER("SD", "Sundry Debtors", "SUNDRY-DEBTORS", "ASSET", "ACCOUNTS_RECEIVABLE"),
        VENDOR("SC", "Sundry Creditors", "SUNDRY-CREDITORS", "LIABILITY", "ACCOUNTS_PAYABLE"),
        GROWER("GRW", "Growers", "GROWERS", "LIABILITY", null),
        ORGANIZER("ORG", "Organizers", "ORGANIZERS", "LIABILITY", null),
        EMPLOYEE("EMP", "Employees", "EMPLOYEES", "LIABILITY", null);

        final String prefix;
        final String groupName;
        final String groupCode;
        final String type;
        final String subType;
        PartyType(String prefix, String groupName, String groupCode, String type, String subType) {
            this.prefix = prefix; this.groupName = groupName; this.groupCode = groupCode;
            this.type = type; this.subType = subType;
        }
    }

    /**
     * Ensure a ledger exists for the party and carries its current name. No-op when the name or id is
     * missing. Safe to call on both create and update.
     */
    /**
     * The stable per-party ledger code, derived from the party's id so it survives renames and is the
     * same value {@link #ensureLedger} uses. Callers that post to a party ledger (e.g. sales invoices)
     * should resolve the account by this code rather than by name, to avoid creating ghost duplicates.
     */
    public String ledgerCode(PartyType party, UUID partyId) {
        return party.prefix + "-" + partyId.toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureLedger(PartyType party, String name, UUID partyId, String partyCode) {
        if (party == null || partyId == null || name == null || name.isBlank()) return;
        UUID tenantId;
        try { tenantId = tenantContext.current(); } catch (Exception e) { return; }
        String code = ledgerCode(party, partyId);
        try {
            AccountGroup group = resolveGroup(tenantId, party);
            if (group == null) return;
            var existing = accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code);
            if (existing.isPresent()) {
                Account a = existing.get();
                boolean dirty = false;
                if (!name.equals(a.getName())) { a.setName(name); dirty = true; }
                if (a.getGroupCode() == null) { a.setGroupCode(group.getCode()); a.setGroupName(group.getName()); dirty = true; }
                if (party.subType != null && a.getSubType() == null) { a.setSubType(party.subType); dirty = true; }
                if (dirty) accountRepository.save(a);
                return;
            }
            Account a = new Account();
            a.setTenantId(tenantId);
            a.setCode(code);
            a.setLedgerCode(partyCode != null && !partyCode.isBlank() ? partyCode : code);
            a.setName(name);
            a.setType(party.type);
            a.setSubType(party.subType);
            a.setGroupCode(group.getCode());
            a.setGroupName(group.getName());
            a.setActive(true);
            a.setBalance(BigDecimal.ZERO);
            accountRepository.saveAndFlush(a);
            log.info("Auto-created {} ledger '{}' ({}) under group {}", party, name, code, group.getCode());
        } catch (Exception e) {
            // Maintaining a ledger is a convenience; it must never be the reason a grower, customer
            // or employee cannot be saved. The writes above flush inside this block precisely so a
            // constraint failure is caught here — left to flush at commit it would escape past this
            // catch and be reported against the record the user was actually creating.
            log.warn("Could not auto-maintain {} ledger for '{}': {}", party, name, e.getMessage());
        }
    }

    /** Find the party's account group by name, else create it so ledgers are correctly grouped. */
    private AccountGroup resolveGroup(UUID tenantId, PartyType party) {
        var byName = accountGroupRepository.findFirstByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, party.groupName);
        if (byName.isPresent()) return byName.get();
        var byCode = accountGroupRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, party.groupCode);
        if (byCode.isPresent()) return byCode.get();

        // The unique index on (tenant_id, code) counts soft-deleted rows, but the lookups above do
        // not — so a group that was deleted still owns its code while looking absent. Inserting
        // alongside it violates the constraint, and because this runs in its own transaction the
        // failure lands at commit, outside the catch below, and surfaced as "a record with these
        // details already exists" on the grower or organizer being saved. Revive it instead.
        var deleted = accountGroupRepository.findFirstByTenantIdAndCode(tenantId, party.groupCode);
        if (deleted.isPresent()) {
            AccountGroup g = deleted.get();
            g.setDeletedAt(null);
            g.setActive(true);
            if (g.getName() == null || g.getName().isBlank()) g.setName(party.groupName);
            return accountGroupRepository.saveAndFlush(g);
        }

        AccountGroup g = new AccountGroup();
        g.setTenantId(tenantId);
        g.setCode(party.groupCode);
        g.setName(party.groupName);
        g.setGroupType(party.type);
        g.setPrimary(true);
        g.setActive(true);
        return accountGroupRepository.save(g);
    }
}
