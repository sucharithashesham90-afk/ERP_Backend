package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.FieldInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FieldInspectionRepository extends JpaRepository<FieldInspection, UUID> {

    Page<FieldInspection> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<FieldInspection> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
