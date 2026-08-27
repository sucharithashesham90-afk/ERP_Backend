package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProductionArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionAreaRepository extends JpaRepository<ProductionArea, UUID> {

    Page<ProductionArea> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<ProductionArea> findByTenantIdAndDeletedAtIsNullAndActiveTrue(UUID tenantId);

    Optional<ProductionArea> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
