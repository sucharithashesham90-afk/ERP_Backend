package com.erp.platform.modules.planning.repository;

import com.erp.platform.modules.planning.entity.ProductionJobAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionJobAllocationRepository extends JpaRepository<ProductionJobAllocation, UUID> {

    List<ProductionJobAllocation> findByTenantIdAndJobIdAndDeletedAtIsNull(UUID tenantId, UUID jobId);

    Optional<ProductionJobAllocation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
