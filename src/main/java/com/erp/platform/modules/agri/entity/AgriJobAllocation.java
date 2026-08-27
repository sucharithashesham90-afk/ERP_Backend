package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "agri_job_allocations",
        indexes = {
            @Index(name = "idx_job_alloc_tenant", columnList = "tenant_id"),
            @Index(name = "idx_job_alloc_job",    columnList = "job_id")
        })
@Getter
@Setter
public class AgriJobAllocation extends TenantEntity {

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "allocation_type", length = 20)
    private String allocationType;

    @Column(name = "allocatee_id")
    private UUID allocateeId;

    @Column(name = "allocatee_name", length = 200)
    private String allocateeName;

    @Column(name = "quantity_kgs", precision = 12, scale = 3)
    private BigDecimal quantityKgs;

    @Column(name = "acreage_acres", precision = 10, scale = 3)
    private BigDecimal acreageAcres;

    @Column(name = "expected_yield_kgs", precision = 12, scale = 3)
    private BigDecimal expectedYieldKgs;

    @Column(name = "temp_lot_number", length = 50)
    private String tempLotNumber;
}
