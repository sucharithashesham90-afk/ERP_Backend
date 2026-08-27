package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.LotGrowerLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotGrowerLinkRepository extends JpaRepository<LotGrowerLink, UUID> {
    Page<LotGrowerLink> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<LotGrowerLink> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
