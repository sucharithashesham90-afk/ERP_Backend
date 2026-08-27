package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    Page<Holiday> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<Holiday> findByTenantIdAndYearAndDeletedAtIsNull(UUID tenantId, int year);
    Optional<Holiday> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
