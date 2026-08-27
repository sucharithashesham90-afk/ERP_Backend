package com.erp.platform.modules.supplier.repository;

import com.erp.platform.modules.supplier.entity.VendorQualification;
import com.erp.platform.modules.supplier.entity.VendorQualification.QualStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorQualificationRepository extends JpaRepository<VendorQualification, UUID> {

    Page<VendorQualification> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<VendorQualification> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, QualStatus status, Pageable pageable);

    Optional<VendorQualification> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
