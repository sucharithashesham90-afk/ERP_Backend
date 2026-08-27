package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseInvoice;
import com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, UUID> {

    Page<PurchaseInvoice> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<PurchaseInvoice> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, PIStatus status, Pageable pageable);

    Optional<PurchaseInvoice> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT COALESCE(SUM(pi.balanceDue), 0) FROM PurchaseInvoice pi " +
           "WHERE pi.tenantId = :tenantId AND pi.status IN ('APPROVED','PARTIALLY_PAID') " +
           "AND pi.deletedAt IS NULL")
    BigDecimal totalPayable(@Param("tenantId") UUID tenantId);

    @Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.tenantId = :tenantId " +
           "AND pi.status IN (com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus.APPROVED, " +
           "com.erp.platform.modules.purchase.entity.PurchaseInvoice.PIStatus.PARTIALLY_PAID) " +
           "AND pi.deletedAt IS NULL")
    List<PurchaseInvoice> findOutstandingPayables(@Param("tenantId") UUID tenantId);

    List<PurchaseInvoice> findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(UUID tenantId, UUID purchaseOrderId);

    Page<PurchaseInvoice> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);
}
