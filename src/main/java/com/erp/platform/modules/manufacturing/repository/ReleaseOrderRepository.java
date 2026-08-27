package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ReleaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReleaseOrderRepository extends JpaRepository<ReleaseOrder, UUID> {
    Page<ReleaseOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ReleaseOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
