package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "balance_sheet_config",
       indexes = {
           @Index(name = "idx_bsc_tenant", columnList = "tenant_id"),
           @Index(name = "idx_bsc_section", columnList = "tenant_id,bs_section")
       })
@Getter
@Setter
public class BalanceSheetConfig extends TenantEntity {

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "bs_section", nullable = false, length = 50)
    private String bsSection; // FIXED_ASSETS, INVESTMENTS, CURRENT_ASSETS, CURRENT_LIABILITIES, LONG_TERM_LIABILITIES, CAPITAL_RESERVES, CONTINGENT_LIABILITIES

    @Column(name = "display_order")
    private int displayOrder = 0;

    @Column(name = "show_sub_total")
    private boolean showSubTotal = true;

    @Column(name = "active")
    private boolean active = true;
}
