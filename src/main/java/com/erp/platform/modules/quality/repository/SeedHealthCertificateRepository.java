package com.erp.platform.modules.quality.repository;
import java.util.UUID;

import com.erp.platform.modules.quality.entity.SeedHealthCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeedHealthCertificateRepository extends JpaRepository<SeedHealthCertificate, UUID> {
    Page<SeedHealthCertificate> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<SeedHealthCertificate> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
