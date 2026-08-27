package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, UUID> {
    List<AssetAssignment> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<AssetAssignment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
