package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.WorkCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkCenterRepository extends JpaRepository<WorkCenter, UUID> {

    Page<WorkCenter> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<WorkCenter> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<WorkCenter> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
}
