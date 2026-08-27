package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {

    List<BankStatement> findByReconciliationIdOrderByTransactionDate(UUID reconciliationId);

    List<BankStatement> findByReconciliationIdAndMatched(UUID reconciliationId, boolean matched);

    Optional<BankStatement> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
