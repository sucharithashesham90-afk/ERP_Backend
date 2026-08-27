package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.NetSalesClosing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NetSalesClosingRepository extends JpaRepository<NetSalesClosing, UUID> {
    List<NetSalesClosing> findByTenantIdAndDeletedAtIsNullOrderByClosedAtDesc(UUID tenantId);
    Optional<NetSalesClosing> findByTenantIdAndSeasonPeriodIdAndDeletedAtIsNull(UUID tenantId, UUID seasonPeriodId);
    Optional<NetSalesClosing> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
