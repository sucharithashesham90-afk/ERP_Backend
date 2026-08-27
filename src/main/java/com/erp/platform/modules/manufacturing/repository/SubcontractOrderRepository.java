package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.SubcontractOrder;
import com.erp.platform.modules.manufacturing.entity.SubcontractOrder.SCOStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubcontractOrderRepository extends JpaRepository<SubcontractOrder, UUID> {

    Page<SubcontractOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<SubcontractOrder> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, SCOStatus status, Pageable pageable);

    Optional<SubcontractOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT COALESCE(COUNT(s), 0) FROM SubcontractOrder s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL")
    long countByTenantId(@Param("tenantId") UUID tenantId);
}
