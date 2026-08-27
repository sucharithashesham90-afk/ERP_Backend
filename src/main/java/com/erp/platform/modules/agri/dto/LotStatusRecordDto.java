package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LotStatusRecordDto {

    private UUID id;
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
    private LocalDateTime createdAt;
}
