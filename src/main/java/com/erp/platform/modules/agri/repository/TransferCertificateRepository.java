package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.TransferCertificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferCertificateRepository extends JpaRepository<TransferCertificate, UUID> {
    Page<TransferCertificate> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<TransferCertificate> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
