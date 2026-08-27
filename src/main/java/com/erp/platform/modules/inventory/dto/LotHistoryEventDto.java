package com.erp.platform.modules.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LotHistoryEventDto {
    private String eventType;       // PRODUCTION_JOB, PROCESS_JOB, QUALITY_INSPECTION, SALES_DISPATCH, SALES_RETURN
    private String eventDate;
    private String referenceNumber;
    private UUID referenceId;
    private String title;
    private String description;
    private String status;
    private BigDecimal quantity;
    private String unit;
    private String partyName;
    private String result;
    private String processType;
    private String inspectionType;
    private String inspectorName;
}
