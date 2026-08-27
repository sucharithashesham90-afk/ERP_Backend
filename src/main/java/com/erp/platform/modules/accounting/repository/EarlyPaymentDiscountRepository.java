package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.EarlyPaymentDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EarlyPaymentDiscountRepository extends JpaRepository<EarlyPaymentDiscount, UUID> {

    Page<EarlyPaymentDiscount> findByTenantIdAndApplied(UUID tenantId, boolean applied, Pageable pageable);

    List<EarlyPaymentDiscount> findByTenantIdAndCustomerIdAndApplied(UUID tenantId, UUID customerId, boolean applied);

    Optional<EarlyPaymentDiscount> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<EarlyPaymentDiscount> findByTenantIdAndAppliedAndDiscountEligibleUntilGreaterThanEqual(
            UUID tenantId, boolean applied, LocalDate date);
}
