package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.AgriProductionJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgriProductionJobRepository extends JpaRepository<AgriProductionJob, UUID> {

    Page<AgriProductionJob> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<AgriProductionJob> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
