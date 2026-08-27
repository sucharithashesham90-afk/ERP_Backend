package com.erp.platform.modules.supplier.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vendor_qualifications",
       indexes = {
           @Index(name = "idx_vq_tenant", columnList = "tenant_id"),
           @Index(name = "idx_vq_vendor", columnList = "tenant_id, vendor_id")
       })
@Getter
@Setter
public class VendorQualification extends TenantEntity {

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "submission_date")
    private LocalDate submissionDate;

    @Column(name = "evaluation_date")
    private LocalDate evaluationDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QualStatus status = QualStatus.PENDING;

    @Column(name = "technical_score")
    private int technicalScore;   // 0-100

    @Column(name = "financial_score")
    private int financialScore;   // 0-100

    @Column(name = "compliance_score")
    private int complianceScore;  // 0-100

    @Column(name = "overall_score")
    private int overallScore;     // computed average

    @Column(length = 500)
    private String certifications; // comma-separated list: ISO9001, ISO14001, etc.

    @Column(name = "evaluated_by", length = 100)
    private String evaluatedBy;

    @Column(length = 1000)
    private String notes;

    public enum QualStatus {
        PENDING, UNDER_REVIEW, APPROVED, REJECTED, EXPIRED
    }
}
