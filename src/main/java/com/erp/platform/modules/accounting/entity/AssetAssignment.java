package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "asset_assignments", indexes = {@Index(name = "idx_asset_assignment_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AssetAssignment extends TenantEntity {

    @Column(length = 200)
    private String assetName;

    @Column(length = 200)
    private String employeeName;

    private LocalDate issueDate;

    private LocalDate returnDate;

    @Column(length = 50)
    private String status = "ISSUED";

    @Column(length = 500)
    private String remarks;
}
