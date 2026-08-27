package com.erp.platform.modules.fieldiot.repository;

import com.erp.platform.modules.fieldiot.entity.FieldPlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldPlotRepository extends JpaRepository<FieldPlot, UUID> {

    Page<FieldPlot> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<FieldPlot> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    List<FieldPlot> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    /**
     * Tenants with at least one active field, for the scheduled sync.
     *
     * <p>A scheduled job runs outside any request, so there is no tenant on the context to read.
     * Rather than sync a tenant that has nothing mapped, this asks the data which tenants are
     * actually using the feature.
     */
    @org.springframework.data.jpa.repository.Query(
            "select distinct f.tenantId from IotFieldPlot f where f.active = true and f.deletedAt is null")
    List<UUID> findTenantIdsWithActiveFields();

    Optional<FieldPlot> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String code);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
