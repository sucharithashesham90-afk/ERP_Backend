package com.erp.platform.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateMaterialTransferNoteRequest {

    @NotBlank
    private String mtnNumber;

    private LocalDate mtnDate;
    private String fromLocation;
    private String toLocation;
    private String purpose;
    private UUID productId;
    private String productName;
    private BigDecimal quantityKgs;
    private String status;
    private String approvedBy;
    private String remarks;
}
