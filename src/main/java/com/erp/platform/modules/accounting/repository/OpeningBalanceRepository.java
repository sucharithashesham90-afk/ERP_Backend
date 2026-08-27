package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.OpeningBalance;
import com.erp.platform.modules.accounting.entity.OpeningBalance.BalanceType;
import com.erp.platform.modules.accounting.entity.OpeningBalance.MigrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, UUID> {

    Page<OpeningBalance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<OpeningBalance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<OpeningBalance> findByTenantIdAndMigrationRefAndDeletedAtIsNull(UUID tenantId, String migrationRef, Pageable pageable);

    List<OpeningBalance> findByTenantIdAndMigrationRefAndDeletedAtIsNull(UUID tenantId, String migrationRef);

    Page<OpeningBalance> findByTenantIdAndBalanceTypeAndDeletedAtIsNull(UUID tenantId, BalanceType balanceType, Pageable pageable);

    Page<OpeningBalance> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, MigrationStatus status, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
