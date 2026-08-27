package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.CostCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {

    Page<CostCenter> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CostCenter> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<CostCenter> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);

    @Query("SELECT c FROM CostCenter c WHERE c.tenantId = :tenantId AND c.parent.id = :parentId AND c.deletedAt IS NULL")
    List<CostCenter> findByTenantIdAndParentIdAndDeletedAtIsNull(UUID tenantId, UUID parentId);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeAndIdNotAndDeletedAtIsNull(UUID tenantId, String code, UUID id);
}
