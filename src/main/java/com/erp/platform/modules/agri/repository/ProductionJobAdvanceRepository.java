package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProductionJobAdvance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductionJobAdvanceRepository extends JpaRepository<ProductionJobAdvance, UUID> {

    Page<ProductionJobAdvance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<ProductionJobAdvance> findByTenantIdAndJobIdAndDeletedAtIsNull(UUID tenantId, UUID jobId);

    List<ProductionJobAdvance> findByTenantIdAndAllocateeIdAndDeletedAtIsNull(UUID tenantId, UUID allocateeId);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
