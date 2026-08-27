package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity(name = "AgriSeedHealthCertificate")
@Table(name = "agri_seed_health_certificates", indexes = {
    @Index(name = "idx_seed_health_cert_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class SeedHealthCertificate extends TenantEntity {

    @Column(name = "certificate_number", length = 100, nullable = false)
    private String certificateNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "issuing_authority", length = 200)
    private String issuingAuthority;

    @Column(name = "testing_laboratory", length = 200)
    private String testingLaboratory;

    @Column(name = "health_status", length = 50)
    private String healthStatus;

    @Column(name = "treatment_required")
    private Boolean treatmentRequired = false;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status = "VALID";
}
