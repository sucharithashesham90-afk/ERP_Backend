package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.QualityCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QualityCheckRepository extends JpaRepository<QualityCheck, UUID> {

    Page<QualityCheck> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<QualityCheck> findByTenantIdAndResultAndDeletedAtIsNull(UUID tenantId, String result, Pageable pageable);

    Optional<QualityCheck> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndWorkOrderIdAndResultAndDeletedAtIsNull(UUID tenantId, UUID workOrderId, String result);
}
