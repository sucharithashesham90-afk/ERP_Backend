package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.WorkingDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingDayRepository extends JpaRepository<WorkingDay, UUID> {
    Page<WorkingDay> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<WorkingDay> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<WorkingDay> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
