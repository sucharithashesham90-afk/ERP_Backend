package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cost_center_allocations",
       indexes = {
           @Index(name = "idx_cca_tenant", columnList = "tenant_id"),
           @Index(name = "idx_cca_cost_center", columnList = "cost_center_id")
       })
@Getter
@Setter
public class CostCenterAllocation extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "children", "parent"})
    private CostCenter costCenter;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "allocation_date")
    private LocalDate allocationDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String description;
}
