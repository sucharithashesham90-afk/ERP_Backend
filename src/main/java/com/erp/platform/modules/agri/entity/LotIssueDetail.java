package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriLotIssueDetail")
@Table(name = "agri_lot_issue_details", indexes = {@Index(name = "idx_lid_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LotIssueDetail extends TenantEntity {

    @Column(name = "issue_number", length = 50, nullable = false)
    private String issueNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "godown", length = 100)
    private String godown;

    @Column(name = "material_group", length = 100)
    private String materialGroup;

    @Column(name = "material_item", length = 200)
    private String materialItem;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "issue_type", length = 50)
    private String issueType = "PROCESSING";

    @Column(name = "remarks", length = 500)
    private String remarks;
}
