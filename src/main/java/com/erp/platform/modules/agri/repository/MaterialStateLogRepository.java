package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.MaterialStateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialStateLogRepository extends JpaRepository<MaterialStateLog, UUID> {
    Page<MaterialStateLog> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<MaterialStateLog> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
