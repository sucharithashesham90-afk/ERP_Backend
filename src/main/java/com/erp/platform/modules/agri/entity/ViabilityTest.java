package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "viability_tests",
       indexes = {@Index(name = "idx_viab_test_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ViabilityTest extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String testNumber;

    @Column(length = 50)
    private String lotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_variant_id")
    private PlantVariant plantVariant;

    @Column(nullable = false)
    private LocalDate testDate;

    @Column
    private LocalDate resultDate;

    @Column(length = 50)
    private String testLab;

    @Column(length = 100)
    private String testedBy;

    /** Viability/germination rate result (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal viabilityRate;

    /** Quality/grade test result (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal qualityGrade;

    /** Moisture content (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal moistureContent;

    /** Inert matter (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal inertMatter;

    /** Other seeds (%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal otherSeeds;

    @Column(length = 20)
    private String testResult; // PASS, FAIL, PENDING

    @Column(length = 30)
    private String status; // PENDING, IN_PROGRESS, COMPLETED, CANCELLED

    /** Sample size in grams */
    @Column(precision = 10, scale = 3)
    private BigDecimal sampleSize;

    @Column(length = 500)
    private String remarks;
}
