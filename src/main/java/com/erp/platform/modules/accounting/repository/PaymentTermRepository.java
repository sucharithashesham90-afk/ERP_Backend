package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.PaymentTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, UUID> {

    Page<PaymentTerm> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<PaymentTerm> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<PaymentTerm> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);
}
