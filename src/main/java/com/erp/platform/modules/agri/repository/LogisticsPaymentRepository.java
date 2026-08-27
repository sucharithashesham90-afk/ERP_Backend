package com.erp.platform.modules.agri.repository;

import com.erp.platform.modules.agri.entity.LogisticsPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogisticsPaymentRepository extends JpaRepository<LogisticsPayment, UUID> {
    List<LogisticsPayment> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<LogisticsPayment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<LogisticsPayment> findByTenantIdAndFieldProducerIdAndDeletedAtIsNull(UUID tenantId, UUID fieldProducerId);
}
