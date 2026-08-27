package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationTypeRepository extends JpaRepository<OperationType, UUID> {

    Page<OperationType> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<OperationType> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<OperationType> findByTenantIdAndActiveAndDeletedAtIsNull(UUID tenantId, boolean active);
}
