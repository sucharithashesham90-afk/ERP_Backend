package com.erp.platform.modules.fieldiot.repository;

import com.erp.platform.modules.fieldiot.entity.IotDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IotDeviceRepository extends JpaRepository<IotDevice, UUID> {

    Page<IotDevice> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    List<IotDevice> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<IotDevice> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<IotDevice> findByTenantIdAndDeviceCodeIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String deviceCode);

    List<IotDevice> findByTenantIdAndFieldPlotIdAndDeletedAtIsNull(UUID tenantId, UUID fieldPlotId);
}
