package com.erp.platform.modules.quality.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "test_result_details",
       indexes = {@Index(name = "idx_test_result_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class TestResultDetail extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_batch_id", nullable = false)
    @JsonIgnore
    private TestBatch testBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parameter_id", nullable = false)
    private TestParameter parameter;

    @Column(name = "parameter_name", length = 200)
    private String parameterName;

    @Column(name = "observed_value", length = 500)
    private String observedValue;

    @Column(name = "numeric_value", precision = 18, scale = 4)
    private BigDecimal numericValue;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ParameterResult result = ParameterResult.NOT_TESTED;

    @Column(length = 1000)
    private String remarks;

    public enum ParameterResult {
        PASS, FAIL, NOT_TESTED
    }
}
