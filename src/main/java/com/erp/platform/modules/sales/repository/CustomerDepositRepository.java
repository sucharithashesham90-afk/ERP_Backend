package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.CustomerDeposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDepositRepository extends JpaRepository<CustomerDeposit, UUID> {

    Page<CustomerDeposit> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<CustomerDeposit> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
