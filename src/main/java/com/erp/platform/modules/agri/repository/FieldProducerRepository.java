package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.FieldProducer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldProducerRepository extends JpaRepository<FieldProducer, UUID> {
    List<FieldProducer> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<FieldProducer> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
