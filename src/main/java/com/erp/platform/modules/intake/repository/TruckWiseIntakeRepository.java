package com.erp.platform.modules.intake.repository;

import com.erp.platform.modules.intake.entity.TruckWiseIntake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TruckWiseIntakeRepository extends JpaRepository<TruckWiseIntake, UUID> {

    Page<TruckWiseIntake> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<TruckWiseIntake> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}
