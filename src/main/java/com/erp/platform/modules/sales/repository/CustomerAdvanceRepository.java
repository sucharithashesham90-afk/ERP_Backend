package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.CustomerAdvance;
import com.erp.platform.modules.sales.entity.CustomerAdvance.AdvanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAdvanceRepository extends JpaRepository<CustomerAdvance, UUID> {

    Page<CustomerAdvance> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CustomerAdvance> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<CustomerAdvance> findByTenantIdAndCustomerIdAndStatusAndDeletedAtIsNull(
            UUID tenantId, UUID customerId, AdvanceStatus status);

    @Query("SELECT COALESCE(SUM(a.amountAvailable), 0) FROM CustomerAdvance a " +
           "WHERE a.tenantId = :tenantId AND a.customerId = :customerId " +
           "AND a.status IN ('AVAILABLE','PARTIALLY_APPLIED') AND a.deletedAt IS NULL")
    BigDecimal totalAvailableForCustomer(@Param("tenantId") UUID tenantId,
                                         @Param("customerId") UUID customerId);
}
