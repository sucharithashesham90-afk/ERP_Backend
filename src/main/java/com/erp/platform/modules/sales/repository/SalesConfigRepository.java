package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesConfigRepository extends JpaRepository<SalesConfig, UUID> {
    List<SalesConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<SalesConfig> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    boolean existsByTenantIdAndConfigKeyAndDeletedAtIsNull(UUID tenantId, String configKey);
}
