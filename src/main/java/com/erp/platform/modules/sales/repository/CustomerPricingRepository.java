package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.CustomerPricing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerPricingRepository extends JpaRepository<CustomerPricing, UUID> {
    List<CustomerPricing> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<CustomerPricing> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
