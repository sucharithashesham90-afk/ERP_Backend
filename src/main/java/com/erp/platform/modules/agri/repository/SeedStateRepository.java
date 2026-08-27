package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SeedState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeedStateRepository extends JpaRepository<SeedState, UUID> {

    List<SeedState> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<SeedState> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SeedState> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
