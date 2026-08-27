package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.QualityInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QualityInspectionRepository extends JpaRepository<QualityInspection, UUID> {
    Page<QualityInspection> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<QualityInspection> findByTenantIdAndInspectionTypeAndDeletedAtIsNull(UUID tenantId, String type, Pageable pageable);
    Optional<QualityInspection> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<QualityInspection> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);
}
