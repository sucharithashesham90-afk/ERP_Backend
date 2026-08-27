package com.erp.platform.modules.quality.repository;
import java.util.UUID;

import com.erp.platform.modules.quality.entity.QualityStandard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualityStandardRepository extends JpaRepository<QualityStandard, UUID> {
    Page<QualityStandard> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<QualityStandard> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
