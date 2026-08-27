package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SeedClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeedClassRepository extends JpaRepository<SeedClass, UUID> {

    List<SeedClass> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<SeedClass> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SeedClass> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
