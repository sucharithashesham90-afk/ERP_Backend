package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.GeoCountry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeoCountryRepository extends JpaRepository<GeoCountry, UUID> {

    Page<GeoCountry> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<GeoCountry> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId);

    Optional<GeoCountry> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<GeoCountry> findByTenantIdAndIsoCode2AndDeletedAtIsNull(UUID tenantId, String isoCode2);
}
