package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    Page<PurchaseOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<PurchaseOrder> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, POStatus status, Pageable pageable);

    Page<PurchaseOrder> findByTenantIdAndVendorIdAndDeletedAtIsNull(UUID tenantId, UUID vendorId, Pageable pageable);

    Page<PurchaseOrder> findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(UUID tenantId, UUID vendorId, POStatus status, Pageable pageable);

    Optional<PurchaseOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<PurchaseOrder> findByTenantIdAndPoNumberIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String poNumber);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, POStatus status);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseOrder p " +
           "WHERE p.tenantId = :tenantId AND p.orderDate >= :from AND p.orderDate <= :to " +
           "AND p.deletedAt IS NULL")
    BigDecimal sumTotalByDateRange(@Param("tenantId") UUID tenantId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);
}
