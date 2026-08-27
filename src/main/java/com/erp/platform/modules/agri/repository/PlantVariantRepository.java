package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PlantVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlantVariantRepository extends JpaRepository<PlantVariant, UUID> {
    List<PlantVariant> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    List<PlantVariant> findByTenantIdAndPlantCategoryIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID categoryId);
    Optional<PlantVariant> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
