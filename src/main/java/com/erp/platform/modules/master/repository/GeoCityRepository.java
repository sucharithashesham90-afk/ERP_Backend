package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.GeoCity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeoCityRepository extends JpaRepository<GeoCity, UUID> {

    Page<GeoCity> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<GeoCity> findByTenantIdAndStateIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID stateId);

    List<GeoCity> findByTenantIdAndCountryIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID countryId);

    List<GeoCity> findByTenantIdAndDistrictIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID districtId);

    Page<GeoCity> findByTenantIdAndDistrictIdAndDeletedAtIsNull(UUID tenantId, UUID districtId, Pageable pageable);

    Optional<GeoCity> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
