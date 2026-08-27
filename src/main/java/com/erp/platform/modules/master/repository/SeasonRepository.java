package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeasonRepository extends JpaRepository<Season, UUID> {

    Page<Season> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Season> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantId(UUID tenantId);
}
