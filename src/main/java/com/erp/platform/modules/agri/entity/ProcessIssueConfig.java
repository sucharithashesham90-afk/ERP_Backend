package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "process_issue_configs", indexes = {@Index(name = "idx_process_issue_config_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessIssueConfig extends TenantEntity {

    @Column(name = "process_type", length = 100, nullable = false)
    private String processType;

    @Column(name = "byproduct_name", length = 100)
    private String byproductName;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @Column(name = "issue_category", length = 50)
    private String issueCategory;
}
