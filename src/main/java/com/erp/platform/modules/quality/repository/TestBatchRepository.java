package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.TestBatch;
import com.erp.platform.modules.quality.entity.TestBatch.TestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestBatchRepository extends JpaRepository<TestBatch, UUID> {

    Page<TestBatch> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<TestBatch> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<TestBatch> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, TestStatus status, Pageable pageable);

    long countByTenantIdAndDeletedAtIsNull(UUID tenantId);
}
