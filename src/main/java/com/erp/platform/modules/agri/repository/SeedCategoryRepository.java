package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.SeedCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeedCategoryRepository extends JpaRepository<SeedCategory, UUID> {

    List<SeedCategory> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<SeedCategory> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SeedCategory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
