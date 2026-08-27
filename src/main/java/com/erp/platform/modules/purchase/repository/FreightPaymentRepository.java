package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.FreightPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreightPaymentRepository extends JpaRepository<FreightPayment, UUID> {
    Page<FreightPayment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<FreightPayment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<FreightPayment> findByTenantIdAndGoodsReceiptIdAndDeletedAtIsNull(UUID tenantId, UUID goodsReceiptId);
}
