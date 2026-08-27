package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.AutoPostVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoPostVoucherRepository extends JpaRepository<AutoPostVoucher, UUID> {
    Page<AutoPostVoucher> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<AutoPostVoucher> findByTenantIdAndActiveTrueAndDeletedAtIsNull(UUID tenantId);
    Optional<AutoPostVoucher> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
