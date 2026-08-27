package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.QualityRejection;
import com.erp.platform.modules.manufacturing.entity.QualityRejection.RejectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QualityRejectionRepository extends JpaRepository<QualityRejection, UUID> {

    Page<QualityRejection> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<QualityRejection> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<QualityRejection> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, RejectionStatus status, Pageable pageable);

    Page<QualityRejection> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);
}
