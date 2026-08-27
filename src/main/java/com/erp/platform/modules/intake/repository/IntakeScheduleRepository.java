package com.erp.platform.modules.intake.repository;

import com.erp.platform.modules.intake.entity.IntakeSchedule;
import com.erp.platform.modules.intake.entity.IntakeSchedule.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntakeScheduleRepository extends JpaRepository<IntakeSchedule, UUID> {

    Page<IntakeSchedule> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<IntakeSchedule> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ScheduleStatus status, Pageable pageable);

    Optional<IntakeSchedule> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);

    @Query("SELECT s FROM IntakeSchedule s WHERE s.tenantId = :tenantId AND s.deletedAt IS NULL AND (s.locationName IN :locations OR s.locationName IS NULL)")
    Page<IntakeSchedule> findByTenantIdAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("locations") List<String> locations, Pageable pageable);

    @Query("SELECT s FROM IntakeSchedule s WHERE s.tenantId = :tenantId AND s.status = :status AND s.deletedAt IS NULL AND (s.locationName IN :locations OR s.locationName IS NULL)")
    Page<IntakeSchedule> findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("status") ScheduleStatus status, @Param("locations") List<String> locations, Pageable pageable);
}
