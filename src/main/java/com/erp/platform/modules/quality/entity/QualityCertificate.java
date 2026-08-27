package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriQualityCertificate")
@Table(name = "agri_quality_certificates", indexes = {@Index(name = "idx_qc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class QualityCertificate extends TenantEntity {

    @Column(name = "certificate_number", length = 50, nullable = false)
    private String certificateNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_name", length = 100)
    private String varietyName;

    @Column(name = "valid_up_to")
    private LocalDate validUpTo;

    @Column(name = "certifying_body", length = 200)
    private String certifyingBody;

    @Column(name = "germination_percent", precision = 6, scale = 2)
    private BigDecimal germinationPercent;

    @Column(name = "purity_percent", precision = 6, scale = 2)
    private BigDecimal purityPercent;

    @Column(name = "moisture_percent", precision = 6, scale = 2)
    private BigDecimal moisturePercent;

    @Column(name = "status", length = 20)
    private String status = "ISSUED";
}
