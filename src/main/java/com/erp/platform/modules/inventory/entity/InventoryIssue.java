package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/** Inventory issue (stock issued out of a godown). Backs /api/v1/inventory/issues. */
@Entity
@Table(name = "inventory_issues",
       indexes = {@Index(name = "idx_invissue_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class InventoryIssue extends TenantEntity {

    @Column(name = "issue_number", length = 50)
    private String issueNumber;

    @Column(length = 150)
    private String location;

    @Column(name = "godown_id")
    private UUID godownId;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(name = "net_id")
    private UUID netId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "issued_by", length = 200)
    private String issuedBy;

    @Column(name = "truck_involved", nullable = false)
    private boolean truckInvolved = false;

    @Column(name = "issue_to", length = 300)
    private String issueTo;

    // Audited quantity issued — set once and never recalculated (audit record, does not drive stock).
    @Column(name = "quantity", precision = 18, scale = 4)
    private java.math.BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(length = 1000)
    private String notes;

    @Column(length = 20)
    private String status = "ISSUED";
}
