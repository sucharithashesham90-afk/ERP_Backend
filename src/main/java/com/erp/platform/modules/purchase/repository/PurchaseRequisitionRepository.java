package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseRequisition;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition.ReqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, UUID> {

    Page<PurchaseRequisition> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PurchaseRequisition> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<PurchaseRequisition> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ReqStatus status, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
