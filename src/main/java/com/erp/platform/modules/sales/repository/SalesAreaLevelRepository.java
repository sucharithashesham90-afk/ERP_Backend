package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesAreaLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesAreaLevelRepository extends JpaRepository<SalesAreaLevel, UUID> {
    Page<SalesAreaLevel> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<SalesAreaLevel> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<SalesAreaLevel> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
