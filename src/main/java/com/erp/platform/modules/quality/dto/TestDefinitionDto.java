package com.erp.platform.modules.quality.dto;

import com.erp.platform.modules.quality.entity.TestDefinition.TestType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TestDefinitionDto {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private TestType testType;
    private String applicableTo;
    private String description;
    private String standardReference;
    private String shortName;
    private int noOfReplications;
    private String testLocationIds;
    private boolean active;
    private LocalDateTime createdAt;
    private List<ParameterDto> parameters;

    @Data
    public static class ParameterDto {
        private UUID id;
        private String parameterName;
        private String shortName;
        private String unit;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private BigDecimal targetValue;
        private String testMethod;
        @JsonProperty("isMandatory")
        private boolean isMandatory;
        private int displayOrder;
        private String resultType;
        private boolean display;
        private boolean resultAffects;
        private boolean keyParam;
    }
}
