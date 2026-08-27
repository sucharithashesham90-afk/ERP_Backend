package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.QualityCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QualityCertificateRepository extends JpaRepository<QualityCertificate, UUID> {
    Page<QualityCertificate> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<QualityCertificate> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
