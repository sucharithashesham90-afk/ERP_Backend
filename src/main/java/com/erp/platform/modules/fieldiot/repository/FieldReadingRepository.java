package com.erp.platform.modules.fieldiot.repository;

import com.erp.platform.modules.fieldiot.entity.FieldReading;
import com.erp.platform.modules.fieldiot.entity.FieldReading.ReadingKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface FieldReadingRepository extends JpaRepository<FieldReading, UUID> {

    Page<FieldReading> findByTenantIdAndDeletedAtIsNullOrderByObservedAtDesc(UUID tenantId, Pageable pageable);

    Page<FieldReading> findByTenantIdAndFieldPlotIdAndDeletedAtIsNullOrderByObservedAtDesc(
            UUID tenantId, UUID fieldPlotId, Pageable pageable);

    Page<FieldReading> findByTenantIdAndFieldPlotIdAndKindAndDeletedAtIsNullOrderByObservedAtDesc(
            UUID tenantId, UUID fieldPlotId, ReadingKind kind, Pageable pageable);

    Page<FieldReading> findByTenantIdAndKindAndDeletedAtIsNullOrderByObservedAtDesc(
            UUID tenantId, ReadingKind kind, Pageable pageable);

    /** Series for one feed on one field, oldest first — backs the trend charts. */
    List<FieldReading> findByTenantIdAndFieldPlotIdAndKindAndObservedAtGreaterThanEqualAndDeletedAtIsNullOrderByObservedAtAsc(
            UUID tenantId, UUID fieldPlotId, ReadingKind kind, LocalDateTime since);

    boolean existsByTenantIdAndFieldPlotIdAndKindAndObservedAtAndDeletedAtIsNull(
            UUID tenantId, UUID fieldPlotId, ReadingKind kind, LocalDateTime observedAt);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
