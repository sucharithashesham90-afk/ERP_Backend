package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.GeoDistrict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeoDistrictRepository extends JpaRepository<GeoDistrict, UUID> {

    Page<GeoDistrict> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<GeoDistrict> findByTenantIdAndStateIdAndDeletedAtIsNull(UUID tenantId, UUID stateId, Pageable pageable);

    List<GeoDistrict> findByTenantIdAndStateIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID stateId);

    List<GeoDistrict> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId);

    Optional<GeoDistrict> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
