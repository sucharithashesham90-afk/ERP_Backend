package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "operation_types",
       indexes = {
           @Index(name = "idx_ot_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ot_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class OperationType extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private OperationCategory category = OperationCategory.MACHINING;

    @Column(nullable = false)
    private boolean active = true;

    public enum OperationCategory {
        MACHINING, ASSEMBLY, TESTING, PACKAGING, TREATMENT, INSPECTION, TRANSPORT, STORAGE
    }
}
