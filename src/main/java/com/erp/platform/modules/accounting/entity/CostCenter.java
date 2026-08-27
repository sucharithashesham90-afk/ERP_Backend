package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A cost centre, optionally under a parent.
 *
 * <p>The class carries {@code @JsonIgnoreProperties} as well as the parent field, and it has to.
 * Creating a child loads its parent as a lazy proxy; the next list query returns that same row from
 * the persistence context — still a proxy — so the proxy is the ROOT object Jackson is asked to
 * write, and a field annotation cannot reach it. Serialising the page then failed on
 * hibernateLazyInitializer, the screen fell back to an empty list, and every existing cost centre
 * appeared to vanish the moment a new one was saved.
 */
@Entity
@Table(name = "cost_centers",
       indexes = {
           @Index(name = "idx_cc_tenant", columnList = "tenant_id"),
           @Index(name = "idx_cc_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CostCenter extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "children"})
    private CostCenter parent;

    @Column(length = 30)
    @Enumerated(EnumType.STRING)
    private CostCenterType type = CostCenterType.DEPARTMENT;

    @Column(name = "budget_amount", precision = 18, scale = 2)
    private BigDecimal budgetAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", precision = 18, scale = 2)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "parent")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "parent"})
    private List<CostCenter> children = new ArrayList<>();

    public enum CostCenterType {
        DEPARTMENT, PROJECT, DIVISION, BRANCH, PRODUCT_LINE
    }
}
