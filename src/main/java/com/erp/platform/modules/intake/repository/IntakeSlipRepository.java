package com.erp.platform.modules.intake.repository;

import com.erp.platform.modules.intake.entity.IntakeSlip;
import com.erp.platform.modules.intake.entity.IntakeSlip.SlipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntakeSlipRepository extends JpaRepository<IntakeSlip, UUID> {

    Page<IntakeSlip> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<IntakeSlip> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    Page<IntakeSlip> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, SlipStatus status, Pageable pageable);

    Optional<IntakeSlip> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
