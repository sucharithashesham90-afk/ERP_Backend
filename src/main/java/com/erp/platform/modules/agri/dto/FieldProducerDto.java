package com.erp.platform.modules.agri.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FieldProducerDto {
    private UUID id;
    private String name;
    private String code;
    private String fatherName;
    private String contactPerson;
    private String phone;
    private String mobile;
    private String address;
    private String village;
    private UUID villageId;
    private String villageName;
    private String district;
    private String state;
    private String country;
    private String postalCode;
    private String landHolding;
    private String accountCode;
    private BigDecimal outstandingBalance;
    private String panNumber;
    private String adharNo;
    private String folioNo;
    private boolean shareholder;
    private boolean organizer;
    private String bankAccount;
    private String bankIfscCode;
    private String bankName;
    private String bankBranch;
    private boolean active;
    private LocalDateTime createdAt;
}
