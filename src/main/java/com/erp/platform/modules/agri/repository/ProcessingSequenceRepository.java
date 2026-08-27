package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProcessingSequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingSequenceRepository extends JpaRepository<ProcessingSequence, UUID> {

    List<ProcessingSequence> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<ProcessingSequence> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<ProcessingSequence> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}
