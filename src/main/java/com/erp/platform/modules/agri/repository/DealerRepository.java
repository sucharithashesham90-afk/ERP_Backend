package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealerRepository extends JpaRepository<Dealer, UUID> {
    List<Dealer> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<Dealer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<Dealer> findByTenantIdAndDealerRegionIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID regionId);
}
