package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    Page<BankAccount> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<BankAccount> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT b FROM BankAccount b WHERE b.tenantId = :tenantId AND b.deletedAt IS NULL AND LOWER(b.accountName) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))")
    Page<BankAccount> search(UUID tenantId, String q, Pageable pageable);
}
