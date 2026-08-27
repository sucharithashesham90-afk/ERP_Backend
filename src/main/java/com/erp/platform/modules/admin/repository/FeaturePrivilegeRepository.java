package com.erp.platform.modules.admin.repository;

import com.erp.platform.modules.admin.entity.FeaturePrivilege;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeaturePrivilegeRepository extends JpaRepository<FeaturePrivilege, UUID> {

    Page<FeaturePrivilege> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<FeaturePrivilege> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Optional<FeaturePrivilege> findByTenantIdAndFeatureKeyAndSubjectTypeAndSubjectIdAndDeletedAtIsNull(
            UUID tenantId, String featureKey, String subjectType, UUID subjectId);
}
