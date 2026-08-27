package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.GeoState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeoStateRepository extends JpaRepository<GeoState, UUID> {

    Page<GeoState> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<GeoState> findByTenantIdAndCountryIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId, UUID countryId);

    List<GeoState> findByTenantIdAndDeletedAtIsNullOrderByNameAsc(UUID tenantId);

    Optional<GeoState> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<GeoState> findByTenantIdAndNameAndCountryIdAndDeletedAtIsNull(UUID tenantId, String name, UUID countryId);
}
