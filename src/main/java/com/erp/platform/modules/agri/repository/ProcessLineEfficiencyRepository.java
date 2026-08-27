package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessLineEfficiency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessLineEfficiencyRepository extends JpaRepository<ProcessLineEfficiency, UUID> {

    Page<ProcessLineEfficiency> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ProcessLineEfficiency> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
