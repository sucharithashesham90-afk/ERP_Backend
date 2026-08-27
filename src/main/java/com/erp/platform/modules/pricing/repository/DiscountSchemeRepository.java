package com.erp.platform.modules.pricing.repository;

import com.erp.platform.modules.pricing.entity.DiscountScheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DiscountSchemeRepository extends JpaRepository<DiscountScheme, UUID> {

    Page<DiscountScheme> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DiscountScheme> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT d FROM DiscountScheme d WHERE d.tenantId = :tenantId AND d.deletedAt IS NULL AND LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))")
    Page<DiscountScheme> search(UUID tenantId, String q, Pageable pageable);
}
