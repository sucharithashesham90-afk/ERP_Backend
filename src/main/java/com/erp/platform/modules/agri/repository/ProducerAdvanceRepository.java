package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProducerAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducerAdvanceRepository extends JpaRepository<ProducerAdvance, UUID> {
    List<ProducerAdvance> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    List<ProducerAdvance> findByTenantIdAndFieldProducerIdAndDeletedAtIsNull(UUID tenantId, UUID producerId);
    Optional<ProducerAdvance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
