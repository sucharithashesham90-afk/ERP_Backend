package com.erp.platform.modules.quality.dto;

import com.erp.platform.modules.quality.entity.TestResultDetail.ParameterResult;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateTestBatchRequest {

    private UUID definitionId;
    private UUID sampleId;
    private LocalDate testDate;
    private String testedBy;
    private String labLocation;
    private String notes;
    private List<ResultDetailRequest> results;

    @Data
    public static class ResultDetailRequest {
        private UUID parameterId;
        private String parameterName;
        private String observedValue;
        private BigDecimal numericValue;
        private ParameterResult result;
        private String remarks;
    }
}
