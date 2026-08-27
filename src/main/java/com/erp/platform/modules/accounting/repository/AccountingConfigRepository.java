package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AccountingConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountingConfigRepository extends JpaRepository<AccountingConfig, UUID> {
    Page<AccountingConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AccountingConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<AccountingConfig> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndConfigKeyAndDeletedAtIsNull(UUID tenantId, String configKey);
}
