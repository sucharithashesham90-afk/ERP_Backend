package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
    Page<LeaveType> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<LeaveType> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<LeaveType> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
