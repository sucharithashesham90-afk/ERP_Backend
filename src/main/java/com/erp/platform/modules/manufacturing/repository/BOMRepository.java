package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.BillOfMaterials;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BOMRepository extends JpaRepository<BillOfMaterials, UUID> {

    Page<BillOfMaterials> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<BillOfMaterials> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<BillOfMaterials> findByTenantIdAndProductIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId, UUID productId);
}
