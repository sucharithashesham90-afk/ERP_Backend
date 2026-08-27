package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProducerContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducerContractRepository extends JpaRepository<ProducerContract, UUID> {
    List<ProducerContract> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    List<ProducerContract> findByTenantIdAndFieldProducerIdAndDeletedAtIsNull(UUID tenantId, UUID producerId);
    Optional<ProducerContract> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
