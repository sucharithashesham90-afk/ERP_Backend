package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.Village;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VillageRepository extends JpaRepository<Village, UUID> {

    List<Village> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<Village> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Village> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
