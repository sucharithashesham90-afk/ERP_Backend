package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ViabilityTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViabilityTestRepository extends JpaRepository<ViabilityTest, UUID> {
    List<ViabilityTest> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    List<ViabilityTest> findByTenantIdAndPlantVariantIdAndDeletedAtIsNull(UUID tenantId, UUID variantId);
    Optional<ViabilityTest> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
