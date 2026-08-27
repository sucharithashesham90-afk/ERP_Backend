package com.erp.platform.modules.payroll.repository;

import com.erp.platform.modules.payroll.entity.SalaryComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, UUID> {

    Page<SalaryComponent> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<SalaryComponent> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);

    List<SalaryComponent> findByTenantIdAndTypeAndActiveTrueAndDeletedAtIsNull(UUID tenantId, String type);

    Optional<SalaryComponent> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
