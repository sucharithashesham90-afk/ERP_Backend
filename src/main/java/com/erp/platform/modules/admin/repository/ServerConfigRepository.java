package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.ServerConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServerConfigRepository extends JpaRepository<ServerConfig, UUID> {

    Page<ServerConfig> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<ServerConfig> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<ServerConfig> findByTenantIdAndCategoryAndDeletedAtIsNull(UUID tenantId, String category);

    Optional<ServerConfig> findByTenantIdAndCategoryAndConfigKeyAndDeletedAtIsNull(UUID tenantId, String category, String configKey);
}
