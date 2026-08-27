package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.TestLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestLocationRepository extends JpaRepository<TestLocation, UUID> {

    List<TestLocation> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<TestLocation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<TestLocation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
