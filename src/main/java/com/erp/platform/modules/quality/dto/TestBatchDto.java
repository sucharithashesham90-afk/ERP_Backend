package com.erp.platform.modules.quality.dto;

import com.erp.platform.modules.quality.entity.TestBatch.TestResult;
import com.erp.platform.modules.quality.entity.TestBatch.TestStatus;
import com.erp.platform.modules.quality.entity.TestResultDetail.ParameterResult;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TestBatchDto {

    private UUID id;
    private UUID tenantId;
    private String batchNumber;
    private UUID definitionId;
    private String definitionName;
    private UUID sampleId;
    private String sampleNumber;
    private LocalDate testDate;
    private String testedBy;
    private String labLocation;
    private TestStatus status;
    private TestResult overallResult;
    private String notes;
    private LocalDateTime createdAt;
    private List<ResultDetailDto> results;

    @Data
    public static class ResultDetailDto {
        private UUID id;
        private UUID parameterId;
        private String parameterName;
        private String observedValue;
        private BigDecimal numericValue;
        private ParameterResult result;
        private String remarks;
    }
}
