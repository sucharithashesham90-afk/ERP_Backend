package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.AgriJobAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgriJobAllocationRepository extends JpaRepository<AgriJobAllocation, UUID> {

    List<AgriJobAllocation> findByTenantIdAndJobIdAndDeletedAtIsNull(UUID tenantId, UUID jobId);

    void deleteByTenantIdAndJobId(UUID tenantId, UUID jobId);
}
