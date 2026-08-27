package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProcessingLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingLineRepository extends JpaRepository<ProcessingLine, UUID> {

    Page<ProcessingLine> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<ProcessingLine> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    Optional<ProcessingLine> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);
}
