package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.CropVarietyTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CropVarietyTestRepository extends JpaRepository<CropVarietyTest, UUID> {

    Page<CropVarietyTest> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CropVarietyTest> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
