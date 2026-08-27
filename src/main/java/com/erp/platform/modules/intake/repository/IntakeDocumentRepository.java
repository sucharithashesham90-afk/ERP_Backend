package com.erp.platform.modules.intake.repository;

import com.erp.platform.modules.intake.entity.IntakeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntakeDocumentRepository extends JpaRepository<IntakeDocument, UUID> {
    Page<IntakeDocument> findByTenantIdAndTypeAndDeletedAtIsNull(UUID tenantId, String type, Pageable pageable);
    Optional<IntakeDocument> findByTenantIdAndIdAndTypeAndDeletedAtIsNull(UUID tenantId, UUID id, String type);
    long countByTenantIdAndTypeAndDeletedAtIsNull(UUID tenantId, String type);
}
