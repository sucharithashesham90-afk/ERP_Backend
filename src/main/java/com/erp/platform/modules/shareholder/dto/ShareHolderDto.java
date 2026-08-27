package com.erp.platform.modules.shareholder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShareHolderDto {
    private UUID id;
    private String shareholderNumber;
    private String name;
    private String address;
    private String email;
    private String phone;
    private String panNumber;
    private String aadharNumber;
    private BigDecimal sharesHeld;
    private BigDecimal faceValuePerShare;
    private LocalDate dateOfAllotment;
    private String nomineeName;
    private String nomineeRelationship;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
