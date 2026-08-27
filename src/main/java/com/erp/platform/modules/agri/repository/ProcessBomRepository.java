package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessBom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessBomRepository extends JpaRepository<ProcessBom, UUID> {

    List<ProcessBom> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<ProcessBom> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<ProcessBom> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
