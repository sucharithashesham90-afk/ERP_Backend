package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Page<PaymentMethod> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<PaymentMethod> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<PaymentMethod> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
