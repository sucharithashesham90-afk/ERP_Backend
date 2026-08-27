package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sample_registrations", indexes = {@Index(name = "idx_sample_registration_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class SampleRegistration extends TenantEntity {

    @Column(name = "sample_number", length = 100)
    private String sampleNumber;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "crop_group_id", length = 100)
    private String cropGroupId;

    @Column(name = "crop_group_name", length = 150)
    private String cropGroupName;

    @Column(name = "crop_id", length = 100)
    private String cropId;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_id", length = 100)
    private String varietyId;

    @Column(name = "variety_name", length = 100)
    private String varietyName;

    @Column(name = "seed_state_id", length = 100)
    private String seedStateId;

    @Column(name = "seed_state_name", length = 100)
    private String seedStateName;

    @Column(name = "crop_variety_test_id", length = 100)
    private String cropVarietyTestId;

    @Column(name = "sample_date")
    private LocalDate sampleDate;

    @Column(name = "test_location_id", length = 100)
    private String testLocationId;

    @Column(name = "test_location_name", length = 150)
    private String testLocationName;

    @Column(name = "sample_weight_grams", precision = 18, scale = 4)
    private BigDecimal sampleWeightGrams;

    @Column(name = "submitted_by", length = 150)
    private String submittedBy;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
