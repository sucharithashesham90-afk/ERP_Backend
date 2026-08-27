package com.erp.platform.modules.dispatch.repository;

import com.erp.platform.modules.dispatch.entity.Dispatch;
import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DispatchRepository extends JpaRepository<Dispatch, UUID> {

    Page<Dispatch> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Dispatch> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<Dispatch> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, DispatchStatus status, Pageable pageable);

    Page<Dispatch> findByTenantIdAndCustomerIdAndDeletedAtIsNull(UUID tenantId, UUID customerId, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
