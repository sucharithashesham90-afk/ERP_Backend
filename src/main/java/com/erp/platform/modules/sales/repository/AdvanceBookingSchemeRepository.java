package com.erp.platform.modules.sales.repository;
import java.util.UUID;

import com.erp.platform.modules.sales.entity.AdvanceBookingScheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvanceBookingSchemeRepository extends JpaRepository<AdvanceBookingScheme, UUID> {
    Page<AdvanceBookingScheme> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AdvanceBookingScheme> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
