package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessIssuance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProcessIssuanceRepository extends JpaRepository<ProcessIssuance, UUID> {
    Page<ProcessIssuance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProcessIssuance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
