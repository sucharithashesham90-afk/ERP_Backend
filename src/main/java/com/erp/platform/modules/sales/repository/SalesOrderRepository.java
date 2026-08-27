package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    Page<SalesOrder> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<SalesOrder> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, SalesOrderStatus status, Pageable pageable);

    Optional<SalesOrder> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, SalesOrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o " +
           "WHERE o.tenantId = :tenantId AND o.orderDate >= :from AND o.orderDate <= :to " +
           "AND o.deletedAt IS NULL")
    BigDecimal sumTotalByDateRange(@Param("tenantId") UUID tenantId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);
}
