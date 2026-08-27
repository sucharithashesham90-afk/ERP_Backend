package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.DealerRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealerRegionRepository extends JpaRepository<DealerRegion, UUID> {
    List<DealerRegion> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<DealerRegion> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
