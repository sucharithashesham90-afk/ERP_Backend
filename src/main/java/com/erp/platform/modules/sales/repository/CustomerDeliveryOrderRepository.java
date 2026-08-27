package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.CustomerDeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDeliveryOrderRepository extends JpaRepository<CustomerDeliveryOrder, UUID> {

    Page<CustomerDeliveryOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CustomerDeliveryOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
