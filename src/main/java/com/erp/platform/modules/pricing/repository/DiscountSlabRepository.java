package com.erp.platform.modules.pricing.repository;

import com.erp.platform.modules.pricing.entity.DiscountSlab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscountSlabRepository extends JpaRepository<DiscountSlab, UUID> {

    List<DiscountSlab> findBySchemeIdOrderByMinValue(UUID schemeId);

    Optional<DiscountSlab> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
