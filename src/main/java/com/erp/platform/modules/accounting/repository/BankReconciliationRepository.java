package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BankReconciliation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID> {

    @EntityGraph(attributePaths = {"bankAccount"})
    Page<BankReconciliation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"bankAccount"})
    Page<BankReconciliation> findByTenantIdAndBankAccountIdAndDeletedAtIsNull(UUID tenantId, UUID bankAccountId, Pageable pageable);

    @EntityGraph(attributePaths = {"bankAccount"})
    Optional<BankReconciliation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
