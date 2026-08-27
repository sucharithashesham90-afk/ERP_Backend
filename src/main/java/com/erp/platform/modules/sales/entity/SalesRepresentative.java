package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_representatives",
       indexes = {
           @Index(name = "idx_salesrep_tenant", columnList = "tenant_id"),
           @Index(name = "idx_salesrep_email", columnList = "tenant_id, email")
       })
@Getter
@Setter
public class SalesRepresentative extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "territory_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SalesTerritory territory;

    @Column(name = "target_amount", precision = 18, scale = 2)
    private BigDecimal targetAmount = BigDecimal.ZERO;

    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;
}
