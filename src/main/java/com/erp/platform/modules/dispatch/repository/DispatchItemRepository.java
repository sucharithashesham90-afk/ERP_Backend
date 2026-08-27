package com.erp.platform.modules.dispatch.repository;

import com.erp.platform.modules.dispatch.entity.DispatchItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DispatchItemRepository extends JpaRepository<DispatchItem, UUID> {

    Page<DispatchItem> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<DispatchItem> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<DispatchItem> findByDispatchId(UUID dispatchId);
}
