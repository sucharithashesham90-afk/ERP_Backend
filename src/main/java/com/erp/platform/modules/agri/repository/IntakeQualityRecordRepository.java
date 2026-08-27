package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.IntakeQualityRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntakeQualityRecordRepository extends JpaRepository<IntakeQualityRecord, UUID> {
    List<IntakeQualityRecord> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    List<IntakeQualityRecord> findByTenantIdAndIntakeSlipIdAndDeletedAtIsNull(UUID tenantId, UUID intakeSlipId);
    Optional<IntakeQualityRecord> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
