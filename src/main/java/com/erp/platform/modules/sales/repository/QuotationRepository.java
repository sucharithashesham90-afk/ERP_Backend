package com.erp.platform.modules.sales.repository;

import com.erp.platform.modules.sales.entity.Quotation;
import com.erp.platform.modules.sales.entity.Quotation.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    Page<Quotation> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Quotation> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, QuotationStatus status, Pageable pageable);

    Optional<Quotation> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, QuotationStatus status);
}
