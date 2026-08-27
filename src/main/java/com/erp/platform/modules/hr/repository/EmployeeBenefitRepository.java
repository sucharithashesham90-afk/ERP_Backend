package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.EmployeeBenefit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefit, UUID> {
    Page<EmployeeBenefit> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<EmployeeBenefit> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<EmployeeBenefit> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
