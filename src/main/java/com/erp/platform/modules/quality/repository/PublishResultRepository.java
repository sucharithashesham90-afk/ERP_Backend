package com.erp.platform.modules.quality.repository;

import com.erp.platform.modules.quality.entity.PublishResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublishResultRepository extends JpaRepository<PublishResult, UUID> {
    Page<PublishResult> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<PublishResult> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<PublishResult> findByTenantIdAndSampleIdInAndDeletedAtIsNull(UUID tenantId, List<UUID> sampleIds);
    Optional<PublishResult> findByTenantIdAndSampleIdAndDeletedAtIsNull(UUID tenantId, UUID sampleId);
}
