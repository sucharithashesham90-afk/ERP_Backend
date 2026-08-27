package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "process_job_input_lots",
       indexes = {
           @Index(name = "idx_pjil_job", columnList = "job_id"),
           @Index(name = "idx_pjil_tenant", columnList = "tenant_id")
       })
@Getter
@Setter
public class ProcessJobInputLot extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private ProductionJob job;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "available_qty", precision = 20, scale = 4)
    private BigDecimal availableQty = BigDecimal.ZERO;

    @Column(name = "to_process", precision = 20, scale = 4)
    private BigDecimal toProcess = BigDecimal.ZERO;

    @Column(name = "complete", nullable = false)
    private boolean complete = false;

    @Column(name = "excess_qty", precision = 20, scale = 4)
    private BigDecimal excessQty = BigDecimal.ZERO;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
