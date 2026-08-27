package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesTerritory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesTerritoryRepository extends JpaRepository<SalesTerritory, UUID> {

    Page<SalesTerritory> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SalesTerritory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<SalesTerritory> findByTenantIdAndDeletedAtIsNullAndActiveTrue(UUID tenantId);

    @Query("SELECT t FROM SalesTerritory t WHERE t.tenantId = :tenantId AND t.deletedAt IS NULL AND LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))")
    Page<SalesTerritory> search(UUID tenantId, String q, Pageable pageable);
}
