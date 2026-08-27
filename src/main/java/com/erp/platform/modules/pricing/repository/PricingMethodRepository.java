package com.erp.platform.modules.pricing.repository;
import com.erp.platform.modules.pricing.entity.PricingMethod;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository("pricingModulePricingMethodRepository")
public interface PricingMethodRepository extends JpaRepository<PricingMethod,UUID> {
    Page<PricingMethod> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<PricingMethod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
