package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    Page<ServiceOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ServiceOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
