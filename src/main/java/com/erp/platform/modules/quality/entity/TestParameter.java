package com.erp.platform.modules.quality.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "test_parameters",
       indexes = {@Index(name = "idx_test_param_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class TestParameter extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "definition_id", nullable = false)
    @JsonIgnore
    private TestDefinition definition;

    @Column(name = "parameter_name", nullable = false, length = 200)
    private String parameterName;

    @Column(length = 50)
    private String unit;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "target_value", precision = 18, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "test_method", length = 200)
    private String testMethod;

    @Column(name = "is_mandatory", nullable = false)
    private boolean isMandatory = true;

    @Column(name = "display_order")
    private int displayOrder;

    @Column(name = "result_type", length = 10)
    private String resultType = "%";

    @Column(name = "display")
    private boolean display = true;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "result_affects")
    private boolean resultAffects = false;

    @Column(name = "key_param")
    private boolean keyParam = false;
}
