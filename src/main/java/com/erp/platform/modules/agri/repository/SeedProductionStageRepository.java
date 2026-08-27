package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SeedProductionStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeedProductionStageRepository extends JpaRepository<SeedProductionStage, UUID> {

    List<SeedProductionStage> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<SeedProductionStage> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SeedProductionStage> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
