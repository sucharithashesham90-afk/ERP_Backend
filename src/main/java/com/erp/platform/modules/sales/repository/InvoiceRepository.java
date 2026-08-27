package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.Invoice;
import com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Invoice> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, InvoiceStatus status, Pageable pageable);

    Optional<Invoice> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.tenantId = :tenantId AND i.invoiceDate >= :from AND i.invoiceDate <= :to " +
           "AND i.deletedAt IS NULL")
    BigDecimal sumTotalByDateRange(@Param("tenantId") UUID tenantId,
                                   @Param("from") LocalDate from,
                                   @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i " +
           "WHERE i.tenantId = :tenantId AND i.status IN ('SENT','PARTIALLY_PAID','OVERDUE') " +
           "AND i.deletedAt IS NULL")
    BigDecimal totalOutstanding(@Param("tenantId") UUID tenantId);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId " +
           "AND i.status IN ('SENT','PARTIALLY_PAID','OVERDUE') " +
           "AND i.balanceDue > 0 AND i.deletedAt IS NULL")
    List<Invoice> findOutstandingInvoices(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(i) FROM Invoice i " +
           "WHERE i.tenantId = :tenantId AND i.invoiceDate >= :from AND i.invoiceDate <= :to " +
           "AND i.deletedAt IS NULL")
    long countByDateRange(@Param("tenantId") UUID tenantId,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);

    @Query("SELECT i.customerName, SUM(i.totalAmount) FROM Invoice i " +
           "WHERE i.tenantId = :tenantId AND i.invoiceDate >= :from AND i.invoiceDate <= :to " +
           "AND i.deletedAt IS NULL GROUP BY i.customerName ORDER BY SUM(i.totalAmount) DESC")
    List<Object[]> topCustomersByRevenue(@Param("tenantId") UUID tenantId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to,
                                         Pageable pageable);

    @Query("SELECT ii.productName, SUM(ii.quantity), SUM(ii.totalAmount) FROM InvoiceItem ii " +
           "JOIN ii.invoice i WHERE i.tenantId = :tenantId AND i.invoiceDate >= :from AND i.invoiceDate <= :to " +
           "AND i.deletedAt IS NULL GROUP BY ii.productName ORDER BY SUM(ii.totalAmount) DESC")
    List<Object[]> topProductsByRevenue(@Param("tenantId") UUID tenantId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to,
                                        Pageable pageable);

    boolean existsByTenantIdAndSalesOrderIdAndDeletedAtIsNull(UUID tenantId, UUID salesOrderId);

    /** Invoices that were never posted to the ledger (e.g. dispatched before auto-posting). Used to backfill. */
    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.posted = false " +
           "AND i.status <> com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus.CANCELLED " +
           "AND i.totalAmount > 0 AND i.deletedAt IS NULL")
    List<Invoice> findUnpostedForBackfill(@Param("tenantId") UUID tenantId);

    /** Used by the overdue-marking scheduler. Finds all unpaid invoices past their due date. */
    @Query("SELECT i FROM Invoice i WHERE i.status IN " +
           "(com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus.SENT, " +
           "com.erp.platform.modules.sales.entity.Invoice.InvoiceStatus.PARTIALLY_PAID) " +
           "AND i.dueDate < :today AND i.deletedAt IS NULL")
    List<Invoice> findInvoicesDueForOverdue(@Param("today") LocalDate today);
}
