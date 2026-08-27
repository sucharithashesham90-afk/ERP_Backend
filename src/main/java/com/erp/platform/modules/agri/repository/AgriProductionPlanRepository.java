package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.AgriProductionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgriProductionPlanRepository extends JpaRepository<AgriProductionPlan, UUID> {
    List<AgriProductionPlan> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<AgriProductionPlan> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<AgriProductionPlan> findByTenantIdAndFieldProducerIdAndDeletedAtIsNull(UUID tenantId, UUID fieldProducerId);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
