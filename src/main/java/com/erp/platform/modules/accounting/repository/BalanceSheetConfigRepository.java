package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BalanceSheetConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceSheetConfigRepository extends JpaRepository<BalanceSheetConfig, UUID> {

    Page<BalanceSheetConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<BalanceSheetConfig> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<BalanceSheetConfig> findByTenantIdAndActiveTrueAndDeletedAtIsNullOrderByBsSectionAscDisplayOrderAsc(UUID tenantId);
}
