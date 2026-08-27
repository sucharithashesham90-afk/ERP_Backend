package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "intake_quality_records",
       indexes = {@Index(name = "idx_iqr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class IntakeQualityRecord extends TenantEntity {

    /** Reference to IntakeSlip id */
    @Column
    private UUID intakeSlipId;

    @Column(length = 50)
    private String intakeSlipNumber;

    @Column
    private LocalDate recordDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_variant_id")
    private PlantVariant plantVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    @Column(precision = 10, scale = 3)
    private BigDecimal grossWeight;

    @Column(precision = 10, scale = 3)
    private BigDecimal moistureDeductionPercent;

    @Column(precision = 10, scale = 3)
    private BigDecimal trashDeductionPercent;

    @Column(precision = 10, scale = 3)
    private BigDecimal netWeight;

    /** Initial viability/quality rate at intake (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal initialViabilityRate;

    /** Initial quality grade at intake (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal initialQualityGrade;

    @Column(length = 20)
    private String qualityStatus; // ACCEPTED, REJECTED, CONDITIONAL

    @Column(length = 500)
    private String remarks;
}
