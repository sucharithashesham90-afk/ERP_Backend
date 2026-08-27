package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PricingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingMethodRepository extends JpaRepository<PricingMethod, UUID> {

    List<PricingMethod> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<PricingMethod> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PricingMethod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
