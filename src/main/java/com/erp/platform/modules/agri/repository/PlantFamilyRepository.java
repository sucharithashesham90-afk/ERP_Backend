package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PlantFamily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlantFamilyRepository extends JpaRepository<PlantFamily, UUID> {
    List<PlantFamily> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<PlantFamily> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
