package com.erp.platform.modules.shareholder.repository;

import com.erp.platform.modules.shareholder.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Page<Coupon> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Coupon> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<Coupon> findByTenantIdAndShareholderIdAndDeletedAtIsNull(UUID tenantId, UUID shareholderId);

    List<Coupon> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, String status);
}
