package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesTarget;
import com.erp.platform.modules.sales.entity.SalesTarget.TargetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesTargetRepository extends JpaRepository<SalesTarget, UUID> {

    Page<SalesTarget> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SalesTarget> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<SalesTarget> findByTenantIdAndDeletedAtIsNullAndStatus(UUID tenantId, TargetStatus status, Pageable pageable);
}
