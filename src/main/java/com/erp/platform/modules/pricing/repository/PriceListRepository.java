package com.erp.platform.modules.pricing.repository;

import com.erp.platform.modules.pricing.entity.PriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PriceListRepository extends JpaRepository<PriceList, UUID> {

    Page<PriceList> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PriceList> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT p FROM PriceList p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))")
    Page<PriceList> search(UUID tenantId, String q, Pageable pageable);

    Optional<PriceList> findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(UUID tenantId);
}
