package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseQuotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseQuotationRepository extends JpaRepository<PurchaseQuotation, UUID> {

    Page<PurchaseQuotation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PurchaseQuotation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<PurchaseQuotation> findByTenantIdAndRequisitionIdAndDeletedAtIsNull(UUID tenantId, UUID requisitionId, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
