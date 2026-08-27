package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An advance paid to a grower or organizer at production-job allocation time, based on the chosen
 * pricing method. Surfaced in the Production Job screen and the grower/organizer drill-down.
 */
@Entity
@Table(name = "production_job_advances",
       indexes = {@Index(name = "idx_pja_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_pja_job", columnList = "job_id"),
                  @Index(name = "idx_pja_allocatee", columnList = "allocatee_id")})
@Getter
@Setter
public class ProductionJobAdvance extends TenantEntity {

    @Column(name = "advance_number", length = 40)
    private String advanceNumber;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "job_number", length = 40)
    private String jobNumber;

    /** GROWER or ORGANIZER. */
    @Column(name = "allocatee_type", length = 20)
    private String allocateeType;

    @Column(name = "allocatee_id")
    private UUID allocateeId;

    @Column(name = "allocatee_name", length = 200)
    private String allocateeName;

    @Column(name = "pricing_method_id")
    private UUID pricingMethodId;

    @Column(name = "pricing_method_name", length = 200)
    private String pricingMethodName;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "advance_date")
    private LocalDate advanceDate;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 500)
    private String remarks;
}
