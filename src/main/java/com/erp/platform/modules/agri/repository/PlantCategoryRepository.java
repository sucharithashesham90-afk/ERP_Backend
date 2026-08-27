package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PlantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlantCategoryRepository extends JpaRepository<PlantCategory, UUID> {
    List<PlantCategory> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    List<PlantCategory> findByTenantIdAndPlantFamilyIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID familyId);
    Optional<PlantCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
