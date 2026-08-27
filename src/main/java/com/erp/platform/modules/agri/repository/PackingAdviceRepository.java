package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PackingAdvice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PackingAdviceRepository extends JpaRepository<PackingAdvice, UUID> {
    Page<PackingAdvice> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PackingAdvice> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
