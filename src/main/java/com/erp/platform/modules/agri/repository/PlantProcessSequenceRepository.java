package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PlantProcessSequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlantProcessSequenceRepository extends JpaRepository<PlantProcessSequence, UUID> {
    List<PlantProcessSequence> findByTenantIdAndActiveTrueAndDeletedAtIsNullOrderBySequenceOrder(UUID tenantId);
    List<PlantProcessSequence> findByTenantIdAndPlantCategoryIdAndActiveTrueAndDeletedAtIsNullOrderBySequenceOrder(UUID tenantId, UUID categoryId);
    Optional<PlantProcessSequence> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
