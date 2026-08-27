package com.erp.platform.modules.planning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Allocation of a production job to a specific grower or organizer.
 * Tracks quantity, acreage, expected yield, advance paid, and temporary lot.
 */
@Entity
@Table(name = "production_job_allocations",
       indexes = {
           @Index(name = "idx_pja_tenant", columnList = "tenant_id"),
           @Index(name = "idx_pja_job", columnList = "tenant_id, job_id"),
       })
@Getter
@Setter
public class ProductionJobAllocation extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private ProductionJob job;

    @Enumerated(EnumType.STRING)
    @Column(name = "allocatee_type", nullable = false, length = 20)
    private AllocateeType allocateeType;

    /** ORGANIZER or STAFF (for organizer allocations) */
    @Column(name = "sub_type", length = 20)
    private String subType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_name", length = 200)
    private String referenceName;

    @Column(name = "quantity_kgs", precision = 15, scale = 3)
    private BigDecimal quantityKgs = BigDecimal.ZERO;

    @Column(name = "acreage_acres", precision = 15, scale = 3)
    private BigDecimal acreageAcres = BigDecimal.ZERO;

    @Column(name = "expected_yield_kgs", precision = 15, scale = 3)
    private BigDecimal expectedYieldKgs = BigDecimal.ZERO;

    @Column(name = "advance_paid", precision = 15, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    /** Auto-generated temporary lot number on initiate */
    @Column(name = "temporary_lot_number", length = 50)
    private String temporaryLotNumber;

    @Column(name = "initiated")
    private boolean initiated = false;

    public enum AllocateeType {
        GROWER, ORGANIZER
    }
}
