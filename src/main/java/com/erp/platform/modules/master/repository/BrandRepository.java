package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Page<Brand> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Brand> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<Brand> findByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    /** Used to enforce the 1:1 brand↔variety rule. */
    List<Brand> findByTenantIdAndVarietyIdAndDeletedAtIsNull(UUID tenantId, String varietyId);

    long countByTenantId(UUID tenantId);
}
