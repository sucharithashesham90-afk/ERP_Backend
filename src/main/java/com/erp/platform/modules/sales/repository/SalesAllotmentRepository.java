package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesAllotment;
import com.erp.platform.modules.sales.entity.SalesAllotment.AllotmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesAllotmentRepository extends JpaRepository<SalesAllotment, UUID> {

    Page<SalesAllotment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<SalesAllotment> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, AllotmentStatus status, Pageable pageable);

    Optional<SalesAllotment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<SalesAllotment> findByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID productId, AllotmentStatus status);

    long countByTenantId(UUID tenantId);
}
