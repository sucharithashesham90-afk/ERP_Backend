package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.BatchHistory;
import com.erp.platform.modules.manufacturing.entity.BatchHistory.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BatchHistoryRepository extends JpaRepository<BatchHistory, UUID> {

    Page<BatchHistory> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<BatchHistory> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<BatchHistory> findByTenantIdAndBatchNumber(UUID tenantId, String batchNumber);

    Page<BatchHistory> findByTenantIdAndProductIdAndDeletedAtIsNull(UUID tenantId, UUID productId, Pageable pageable);

    Page<BatchHistory> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, BatchStatus status, Pageable pageable);
}
