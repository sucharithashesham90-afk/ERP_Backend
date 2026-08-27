package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.InventoryIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InventoryIssueRepository extends JpaRepository<InventoryIssue, UUID> {
    Page<InventoryIssue> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<InventoryIssue> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    long countByTenantId(UUID tenantId);

    // Audit list: never excludes CANCELLED rows; honours optional date/status/location filters.
    @Query("SELECT i FROM InventoryIssue i WHERE i.tenantId = :t AND i.deletedAt IS NULL " +
            "AND (:location IS NULL OR i.location = :location) " +
            "AND (:godownId IS NULL OR i.godownId = :godownId) " +
            "AND (:from IS NULL OR i.issueDate >= :from) " +
            "AND (:to IS NULL OR i.issueDate <= :to) " +
            "AND (:issueNumber IS NULL OR i.issueNumber = :issueNumber) " +
            "AND (:status IS NULL OR i.status = :status) " +
            "ORDER BY i.issueDate DESC, i.createdAt DESC")
    Page<InventoryIssue> search(@Param("t") UUID t, @Param("location") String location,
            @Param("godownId") UUID godownId, @Param("from") LocalDate from, @Param("to") LocalDate to,
            @Param("issueNumber") String issueNumber, @Param("status") String status, Pageable pageable);
}
