package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
    Page<GoodsReceipt> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<GoodsReceipt> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<GoodsReceipt> findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(UUID tenantId, UUID purchaseOrderId);

    /** Guards against raising a second receipt for the same intake slip. */
    boolean existsByTenantIdAndSourceIntakeSlipIdAndDeletedAtIsNull(UUID tenantId, UUID sourceIntakeSlipId);
}
