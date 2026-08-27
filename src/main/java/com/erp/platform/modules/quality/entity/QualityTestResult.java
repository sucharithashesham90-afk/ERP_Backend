package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriQualityTestResult")
@Table(name = "agri_quality_test_results", indexes = {@Index(name = "idx_qtr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class QualityTestResult extends TenantEntity {

    @Column(name = "test_result_number", length = 50, nullable = false)
    private String testResultNumber;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_name", length = 100)
    private String varietyName;

    @Column(name = "test_type", length = 50)
    private String testType;

    @Column(name = "germination_percent", precision = 6, scale = 2)
    private BigDecimal germinationPercent;

    @Column(name = "purity_percent", precision = 6, scale = 2)
    private BigDecimal purityPercent;

    @Column(name = "moisture_percent", precision = 6, scale = 2)
    private BigDecimal moisturePercent;

    @Column(name = "inert_matter_percent", precision = 6, scale = 2)
    private BigDecimal inertMatterPercent;

    @Column(name = "tested_by", length = 100)
    private String testedBy;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "remarks", length = 500)
    private String remarks;
}
