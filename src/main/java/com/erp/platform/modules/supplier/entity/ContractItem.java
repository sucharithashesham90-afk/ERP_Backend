package com.erp.platform.modules.supplier.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "contract_items",
        indexes = {
                @Index(name = "idx_contract_item_contract", columnList = "contract_id")
        })
@Getter
@Setter
public class ContractItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonIgnore
    private SupplierContract contract;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "agreed_price", precision = 18, scale = 2)
    private BigDecimal agreedPrice;

    @Column(name = "min_quantity", precision = 18, scale = 3)
    private BigDecimal minQuantity;

    @Column(name = "max_quantity", precision = 18, scale = 3)
    private BigDecimal maxQuantity;

    @Column(length = 20)
    private String unit;

    @Column(name = "lead_time_days")
    private int leadTimeDays;

    @Column(name = "quality_spec", length = 1000)
    private String qualitySpec;
}
