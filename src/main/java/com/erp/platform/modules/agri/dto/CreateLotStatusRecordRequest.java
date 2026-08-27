package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateLotStatusRecordRequest {

    private String lotNumber;
    private String registrationNumber;
    private String cropName;
    private String varietyName;
    private String fieldProducerName;
    private String currentStatus;
    private String qualityStatus;
    private BigDecimal inventoryKgs;
    private BigDecimal processedKgs;
    private String remarks;
    private LocalDate statusDate;
}
