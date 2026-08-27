package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.ExpectedSales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExpectedSalesRepository extends JpaRepository<ExpectedSales, UUID> {

    Page<ExpectedSales> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ExpectedSales> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
