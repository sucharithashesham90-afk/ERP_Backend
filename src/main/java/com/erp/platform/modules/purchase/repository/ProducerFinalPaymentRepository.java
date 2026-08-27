package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.ProducerFinalPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProducerFinalPaymentRepository extends JpaRepository<ProducerFinalPayment, UUID> {
    Page<ProducerFinalPayment> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProducerFinalPayment> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
