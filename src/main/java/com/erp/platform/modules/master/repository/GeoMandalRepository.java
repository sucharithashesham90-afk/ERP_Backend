package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.GeoMandal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeoMandalRepository extends JpaRepository<GeoMandal, UUID> {

    Page<GeoMandal> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<GeoMandal> findByTenantIdAndDistrictIdAndDeletedAtIsNull(UUID tenantId, UUID districtId, Pageable pageable);

    List<GeoMandal> findByTenantIdAndDistrictIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID districtId);

    List<GeoMandal> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId);

    Optional<GeoMandal> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
