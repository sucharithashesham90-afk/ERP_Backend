package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_batches",
       indexes = {
           @Index(name = "idx_test_batch_tenant", columnList = "tenant_id"),
           @Index(name = "idx_test_batch_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class TestBatch extends TenantEntity {

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    private TestDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "tested_by", length = 100)
    private String testedBy;

    @Column(name = "lab_location", length = 200)
    private String labLocation;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TestStatus status = TestStatus.PENDING;

    @Column(name = "overall_result", length = 20)
    @Enumerated(EnumType.STRING)
    private TestResult overallResult = TestResult.PENDING;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "testBatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResultDetail> results = new ArrayList<>();

    public enum TestStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum TestResult {
        PASS, FAIL, CONDITIONAL, PENDING
    }
}
