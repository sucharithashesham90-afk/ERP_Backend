package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final TenantContext tenantContext;

    public PageResponse<Account> list(String type, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (type != null && !type.isBlank()) {
            return PageResponse.of(
                    accountRepository.findByTenantIdAndTypeAndDeletedAtIsNull(tenantId, type, pageable));
        }
        return PageResponse.of(accountRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public List<Account> getByType(String type) {
        UUID tenantId = tenantContext.current();
        return accountRepository.findByTenantIdAndTypeAndDeletedAtIsNull(tenantId, type);
    }

    public Account getById(UUID id) {
        return findOrThrow(id);
    }

    public Account getByCode(String code) {
        UUID tenantId = tenantContext.current();
        return accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, code)
                .orElseThrow(() -> AppException.notFound("Account not found with code: " + code));
    }

    @Transactional
    public Account create(Account account) {
        UUID tenantId = tenantContext.current();
        accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(tenantId, account.getCode())
                .ifPresent(a -> { throw AppException.conflict("Account with code already exists: " + account.getCode()); });
        account.setTenantId(tenantId);
        if (account.getBalance() == null) account.setBalance(BigDecimal.ZERO);
        Account saved = accountRepository.save(account);
        log.info("Account created: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }

    @Transactional
    public Account update(UUID id, Account body) {
        Account existing = findOrThrow(id);
        existing.setName(body.getName());
        existing.setType(body.getType());
        existing.setSubType(body.getSubType());
        existing.setDescription(body.getDescription());
        existing.setParentId(body.getParentId());
        existing.setActive(body.isActive());
        // Ledger definition fields
        existing.setGroupCode(body.getGroupCode());
        existing.setGroupName(body.getGroupName());
        existing.setLedgerCode(body.getLedgerCode());
        existing.setSalesArea(body.getSalesArea());
        existing.setOverdraft(body.isOverdraft());
        existing.setOverdraftAmount(body.getOverdraftAmount());
        existing.setAccountDivision(body.getAccountDivision());
        existing.setBank(body.isBank());
        existing.setBankCode(body.getBankCode());
        existing.setAccountNo(body.getAccountNo());
        existing.setContra(body.isContra());
        existing.setShowOpeningBalance(body.isShowOpeningBalance());
        existing.setCentral(body.isCentral());
        existing.setSubAccount(body.isSubAccount());
        existing.setLocation(body.getLocation());
        return accountRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = findOrThrow(id);
        if (account.isSystem()) {
            throw AppException.forbidden("Cannot delete a system account");
        }
        account.setDeletedAt(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Account soft-deleted: id={}", id);
    }

    @Transactional
    public void adjustBalance(UUID id, BigDecimal amount) {
        Account account = findOrThrow(id);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private Account findOrThrow(UUID id) {
        return accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Account not found: " + id));
    }
}
