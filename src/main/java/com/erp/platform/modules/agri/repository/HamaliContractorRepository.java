package com.erp.platform.modules.agri.repository;
import java.util.UUID;

import com.erp.platform.modules.agri.entity.HamaliContractor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HamaliContractorRepository extends JpaRepository<HamaliContractor, UUID> {
    Page<HamaliContractor> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<HamaliContractor> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
