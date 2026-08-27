package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.AdminLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminLocationRepository extends JpaRepository<AdminLocation, UUID> {

    Page<AdminLocation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<AdminLocation> findByTenantIdAndActiveAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, boolean active);

    Optional<AdminLocation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /** All active locations of a given type (e.g. PRODUCTION_AREA, PROCESSING_PLANT). */
    List<AdminLocation> findByTenantIdAndLocationTypeAndActiveAndDeletedAtIsNullOrderByNameAsc(
            UUID tenantId, String locationType, boolean active);

    /** Processing Plants that belong to a specific Production Area parent. */
    List<AdminLocation> findByTenantIdAndParentIdAndActiveAndDeletedAtIsNullOrderByNameAsc(
            UUID tenantId, UUID parentId, boolean active);
}
