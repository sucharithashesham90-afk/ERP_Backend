package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.LedgerOpeningBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerOpeningBalanceRepository extends JpaRepository<LedgerOpeningBalance, UUID> {
    List<LedgerOpeningBalance> findByTenantIdAndAccountIdAndDeletedAtIsNull(UUID tenantId, UUID accountId);
}
