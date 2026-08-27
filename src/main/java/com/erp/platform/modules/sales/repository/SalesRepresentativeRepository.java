package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.SalesRepresentative;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesRepresentativeRepository extends JpaRepository<SalesRepresentative, UUID> {

    Page<SalesRepresentative> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<SalesRepresentative> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<SalesRepresentative> findByTenantIdAndTerritoryIdAndDeletedAtIsNull(UUID tenantId, UUID territoryId);

    @Query("SELECT r FROM SalesRepresentative r WHERE r.tenantId = :tenantId AND r.deletedAt IS NULL AND LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))")
    Page<SalesRepresentative> search(UUID tenantId, String q, Pageable pageable);
}
