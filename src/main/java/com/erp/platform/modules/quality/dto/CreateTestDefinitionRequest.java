package com.erp.platform.modules.quality.dto;

import com.erp.platform.modules.quality.entity.TestDefinition.TestType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateTestDefinitionRequest {

    @NotBlank(message = "Test name is required")
    private String name;
    private String code;
    private TestType testType;
    private String applicableTo;
    private String description;
    private String standardReference;
    private String shortName;
    private int noOfReplications;
    private String testLocationIds;
    private boolean active = true;
    private List<ParameterRequest> parameters;

    @Data
    public static class ParameterRequest {
        private String parameterName;
        private String shortName;
        private String unit;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private BigDecimal targetValue;
        private String testMethod;
        @JsonProperty("isMandatory")
        private boolean isMandatory = true;
        private int displayOrder;
        private String resultType = "%";
        private boolean display = true;
        private boolean resultAffects = false;
        private boolean keyParam = false;
    }
}
