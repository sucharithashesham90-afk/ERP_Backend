package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.GroupRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRangeRepository extends JpaRepository<GroupRange, UUID> {
    Page<GroupRange> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    List<GroupRange> findByTenantIdAndDeletedAtIsNull(UUID tenantId);
    Optional<GroupRange> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
