package com.erp.platform.modules.promotions.repository;

import com.erp.platform.modules.promotions.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    Page<Promotion> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Promotion> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<Promotion> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active, Pageable pageable);

    List<Promotion> findByTenantIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndActiveAndDeletedAtIsNull(
            UUID tenantId, LocalDate startDate, LocalDate endDate, boolean active);

    long countByTenantId(UUID tenantId);
}
