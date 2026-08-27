package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    Page<Designation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<Designation> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<Designation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
