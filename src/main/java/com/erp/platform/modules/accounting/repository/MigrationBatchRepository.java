package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.MigrationBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MigrationBatchRepository extends JpaRepository<MigrationBatch, UUID> {

    Page<MigrationBatch> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<MigrationBatch> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<MigrationBatch> findByTenantIdAndBatchNumberAndDeletedAtIsNull(UUID tenantId, String batchNumber);

    long countByTenantId(UUID tenantId);
}
