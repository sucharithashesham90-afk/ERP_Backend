package com.erp.platform.modules.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class MaterialTransferNoteDto {

    private UUID id;
    private String mtnNumber;
    private LocalDate mtnDate;
    private String fromLocation;
    private String toLocation;
    private String purpose;
    private String productName;
    private BigDecimal quantityKgs;
    private String status;
    private String approvedBy;
    private String remarks;
    private LocalDateTime createdAt;
}
