package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.InventoryReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, UUID> {
    Page<InventoryReceipt> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<InventoryReceipt> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    long countByTenantId(UUID tenantId);

    // Audit list: never excludes CANCELLED rows; honours optional date/status/location filters.
    @Query("SELECT r FROM InventoryReceipt r WHERE r.tenantId = :t AND r.deletedAt IS NULL " +
            "AND (:location IS NULL OR r.location = :location) " +
            "AND (:godownId IS NULL OR r.godownId = :godownId) " +
            "AND (:from IS NULL OR r.receiptDate >= :from) " +
            "AND (:to IS NULL OR r.receiptDate <= :to) " +
            "AND (:receiptNumber IS NULL OR r.receiptNumber = :receiptNumber) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "ORDER BY r.receiptDate DESC, r.createdAt DESC")
    Page<InventoryReceipt> search(@Param("t") UUID t, @Param("location") String location,
            @Param("godownId") UUID godownId, @Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("receiptNumber") String receiptNumber, @Param("status") String status, Pageable pageable);
}
