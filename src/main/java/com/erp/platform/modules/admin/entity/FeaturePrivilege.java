package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Grants access to a feature at either a GROUP or a USER level. Backs the Feature Privileges screen
 * (Admin → System Configuration): a feature can be granted to a group or a specific user, and revoked.
 */
@Entity
@Table(name = "feature_privileges",
       indexes = {@Index(name = "idx_featpriv_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_featpriv_subject", columnList = "subject_type, subject_id")})
@Getter
@Setter
public class FeaturePrivilege extends TenantEntity {

    @Column(name = "feature_key", nullable = false, length = 150)
    private String featureKey;

    @Column(name = "feature_name", length = 200)
    private String featureName;

    /** GROUP or USER. */
    @Column(name = "subject_type", nullable = false, length = 10)
    private String subjectType;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "subject_name", length = 200)
    private String subjectName;

    @Column(nullable = false)
    private boolean granted = true;
}
