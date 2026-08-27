package com.erp.platform.modules.pricing.repository;
import com.erp.platform.modules.pricing.entity.PricingScheme;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; import java.util.UUID;
public interface PricingSchemeRepository extends JpaRepository<PricingScheme,UUID> {
    Page<PricingScheme> findByTenantIdAndDeletedAtIsNull(UUID tenantId,Pageable pageable);
    Optional<PricingScheme> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId,UUID id);
}
