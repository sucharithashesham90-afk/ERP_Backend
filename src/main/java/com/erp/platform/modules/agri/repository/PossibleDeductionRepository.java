package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.PossibleDeduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PossibleDeductionRepository extends JpaRepository<PossibleDeduction, UUID> {

    List<PossibleDeduction> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Page<PossibleDeduction> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PossibleDeduction> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
