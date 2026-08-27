package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "asset_purchase_definitions",
       indexes = {
           @Index(name = "idx_apd_tenant", columnList = "tenant_id"),
           @Index(name = "idx_apd_code",   columnList = "tenant_id,code", unique = true)
       })
@Getter
@Setter
public class AssetPurchaseDefinition extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "asset_group_code", length = 30)
    private String assetGroupCode;

    @Column(name = "purchase_account_code", length = 30)
    private String purchaseAccountCode;

    @Column(name = "acc_depreciation_account_code", length = 30)
    private String accDepreciationAccountCode;

    @Column(name = "depreciation_expense_code", length = 30)
    private String depreciationExpenseCode;

    @Column(name = "min_capitalization_amount", precision = 20, scale = 4)
    private BigDecimal minCapitalizationAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
