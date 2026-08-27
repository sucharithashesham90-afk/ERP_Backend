package com.erp.platform.modules.inventory.dto;

import com.erp.platform.modules.inventory.entity.ScrapDisposal.DisposalMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ScrapDisposalDto {

    private UUID id;
    private LocalDate disposalDate;
    private DisposalMethod disposalMethod;
    private String disposedTo;
    private BigDecimal disposalAmount;
    private BigDecimal transportCost;
    private BigDecimal netRecovery;
    private String referenceNumber;
    private String notes;
}
