package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.ProducerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducerPaymentRepository extends JpaRepository<ProducerPayment, UUID> {
    List<ProducerPayment> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<ProducerPayment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<ProducerPayment> findByTenantIdAndFieldProducerIdAndDeletedAtIsNull(UUID tenantId, UUID fieldProducerId);
}
